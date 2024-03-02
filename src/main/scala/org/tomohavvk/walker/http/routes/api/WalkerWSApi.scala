package org.tomohavvk.walker.http.routes.api

import cats.Monad
import cats.effect._
import cats.syntax.all._
import fs2.Stream
import fs2.concurrent.Topic
import fs2.text.utf8
import io.odin.Logger
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.HttpRoutes
import org.http4s._
import org.tomohavvk.walker.handlers.WalkerWSMessageHandler
import org.tomohavvk.walker.http.routes.api.WalkerWSApi.NoMessage
import org.tomohavvk.walker.http.routes.api.WalkerWSApi.WSSubscribers
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.BadRequestError
import org.tomohavvk.walker.serialization.json.ProtocolSerialization
import org.tomohavvk.walker.services.DeviceService
import org.tomohavvk.walker.utils.UnliftF
import io.circe.syntax.EncoderOps
import io.circe.parser.decode
import org.tomohavvk.walker.protocol.ws.WSError
import org.tomohavvk.walker.protocol.ws.WSMessageIn
import org.tomohavvk.walker.protocol.ws.WSMessageOut

class WalkerWSApi[F[_]: Monad, M[_]: Monad](
  deviceService:  DeviceService[F],
  messageHandler: WalkerWSMessageHandler[F],
  subscribers:    WSSubscribers[M],
  loggerM:        Logger[M]
)(implicit
  C: Concurrent[M],
  U: UnliftF[F, M, AppError])
    extends Http4sDsl[M]
    with ProtocolSerialization {

  def wsRoute(wsb: WebSocketBuilder2[M]): HttpRoutes[M] =
    HttpRoutes.of[M] {
      case req @ GET -> Root / "api" / "v1" / "ws" / deviceId =>
        loggerM.debug(s"Handle WS handshake with device: $deviceId") >>
          getDevice(deviceId).flatMap {
            case Right(device) =>
              Topic[M, WebSocketFrame].flatMap { topic =>
                wsb.build(topic.subscribe(10), wsStream(device.id, topic, _, subscribers))
              }

            case Left(error) =>
              Response[M](status = Status.NotFound, body = Stream(error.apiMessage.value).through(utf8.encode)).pure[M]
          }
    }

  private def wsStream(
    deviceId:    DeviceId,
    topic:       Topic[M, WebSocketFrame],
    stream:      Stream[M, WebSocketFrame],
    subscribers: WSSubscribers[M]
  ): Stream[M, Unit] =
    Stream.eval(addDevice(deviceId, topic, subscribers)) >>
      stream
        .evalMap[M, Option[String]] {
          case WebSocketFrame.Text(message, _) => handleIncomingMessage(deviceId, message)
          case WebSocketFrame.Close(_)         => handleCloseConnection(deviceId, subscribers)
          case unknown                         => handleUnknownFrame(deviceId, unknown)
        }
        .collect { case Some(message) => message }
        //   .evalTap(x => println(x).pure[M])
        .map(text => WebSocketFrame.Text(text))
        .through(topic.publish)

  private def handleIncomingMessage(deviceId: DeviceId, message: String): M[Option[String]] =
    loggerM
      .debug(s"Handle incoming message from device: ${deviceId.value}") >>
      (message match {
        case "ping" => Option("pong").pure[M]
        case _ =>
          decode[WSMessageIn](message) match {
            case Right(message) =>
              U.unlift(messageHandler.handle(deviceId, message)).flatMap {
                case Right(value) => value.asJson.toString().some.pure[M]
                case Left(error) =>
                  loggerM.error(error.logMessage.value) >>
                    WSError(error.apiMessage.value)
                      .asInstanceOf[WSMessageOut]
                      .asJson(encoderWSMessageOut)
                      .noSpaces
                      .some
                      .pure[M]
              }

            case Left(error) =>
              BadRequestError(error.getLocalizedMessage).asInstanceOf[AppError].asJson.toString().some.pure[M]
          }
      })

  private def handleCloseConnection(
    deviceId:    DeviceId,
    subscribers: WSSubscribers[M]
  ): M[Option[String]] =
    loggerM.debug(s"Handle connection close for device: ${deviceId.value}") >>
      subscribers.ref.get
        .flatMap { subscriptionMap =>
          loggerM.debug(s"Subscription ref size: ${subscriptionMap.size}") >>
            (subscriptionMap.get(deviceId) match {
              case Some(topic) => closeTopic(deviceId, topic) >> removeDevice(deviceId, subscribers)
              case None        => loggerM.warn(s"Can't find topic for device: ${deviceId.value}")
            })
        }
        .as(NoMessage)

  private def handleUnknownFrame(deviceId: DeviceId, unknown: WebSocketFrame): M[Option[String]] =
    loggerM.error(s"Handled unknown frame for device: ${deviceId.value}. Frame: $unknown") >>
      Option.empty[String].pure[M]

  private def closeTopic(deviceId: DeviceId, topic: Topic[M, WebSocketFrame]): M[Unit] =
    topic.close.flatMap[Unit] {
      case Right(_) =>
        loggerM.debug(s"Successfully close the topic for device: ${deviceId.value}")
      case Left(error) =>
        loggerM.error(s"Error during close device topic: ${deviceId.value}. Error: $error")
    }

  private def addDevice(
    deviceId:    DeviceId,
    topic:       Topic[M, WebSocketFrame],
    subscribers: WSSubscribers[M]
  ): M[Unit] =
    subscribers.ref.tryUpdate(_ + (deviceId -> topic)).flatMap {
      case true =>
        loggerM.debug(
          s"Successfully added subscription ref for device: ${deviceId.value}"
        )
      case false =>
        loggerM.error(s"Can't remove topic from subscription ref. Investigation needed")
    }

  private def removeDevice(deviceId: DeviceId, subscribers: WSSubscribers[M]): M[Unit] =
    subscribers.ref.tryUpdate(_ - deviceId).flatMap {
      case true =>
        loggerM.debug(
          s"Successfully remove subscription ref for device: ${deviceId.value}"
        )
      case false =>
        loggerM.error(s"Can't remove topic from subscription ref. Investigation needed")
    }

  private def getDevice(deviceId: String): M[Either[AppError, DeviceEntity]] =
    U.unlift(deviceService.getDevice(DeviceId(deviceId)))
}

object WalkerWSApi {
  val NoMessage: Option[String] = Option.empty[String]

  case class WSSubscribers[M[_]](ref: Ref[M, Map[DeviceId, Topic[M, WebSocketFrame]]])
}
