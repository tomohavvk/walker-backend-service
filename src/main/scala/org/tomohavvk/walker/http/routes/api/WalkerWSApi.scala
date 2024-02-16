package org.tomohavvk.walker.http.routes.api

import cats.Monad
import cats.effect._
import cats.mtl.Handle
import cats.syntax.all._
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import fs2.Stream
import fs2.concurrent.Topic
import io.odin.Logger
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.HttpRoutes
import org.http4s._
import org.tomohavvk.walker.http.routes.api.WalkerWSApi.NoMessage
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.services.DeviceService
import org.tomohavvk.walker.utils.UnliftF

class WalkerWSApi[F[_]: Monad, H[_]: Monad](
  deviceService:   DeviceService[F],
  subscriptionRef: Ref[H, Map[DeviceId, Topic[H, WebSocketFrame]]],
  loggerF:         Logger[F],
  loggerH:         Logger[H]
)(implicit
  C:  Concurrent[H],
  U:  UnliftF[F, H, AppError],
  HF: Handle[F, AppError])
    extends Http4sDsl[H] {

  def wsRoute(wsb: WebSocketBuilder2[H]): HttpRoutes[H] =
    HttpRoutes.of[H] {
      case req @ GET -> Root / "api" / "v1" / "wss" / deviceId =>
        getDevice(deviceId).flatMap {
          case Some(device) =>
            Topic[H, WebSocketFrame].flatMap { topic =>
              subscriptionRef.update(_ + (device.id -> topic)) >>
                wsb.build(topic.subscribe(1000), wsStream(device.id, topic, _))
            }

          case None =>
            Response[H](status = Status.Ok).pure[H]
        }
    }

  private def wsStream(
    deviceId: DeviceId,
    topic:    Topic[H, WebSocketFrame],
    stream:   Stream[H, WebSocketFrame]
  ): Stream[H, Unit] =
    stream
      .evalMap[H, Option[String]] {
        case WebSocketFrame.Text(message, _) => handleIncomingMessage(deviceId, message)
        case WebSocketFrame.Close(_)         => handleCloseConnection(deviceId, subscriptionRef)
        case unknown                         => handleUnknownFrame(deviceId, unknown)
      }
      .collect { case Some(message) => message }
      .map(text => WebSocketFrame.Text(text))
      .through(topic.publish)

  private def handleUnknownFrame(deviceId: DeviceId, unknown: WebSocketFrame): H[Option[String]] =
    loggerH.error(s"Handled unknown frame for device: ${deviceId.value}. Frame: $unknown") >>
      Option.empty[String].pure[H]

  private def handleIncomingMessage(deviceId: DeviceId, message: String): H[Option[String]] =
    loggerH
      .debug(s"Handle incoming message from device: ${deviceId.value}. Message: $message") >>
      (message match {
        case "ping" => Option("pong").pure[H]
        case _      => Option(s"$message:${NanoIdUtils.randomNanoId()}").pure[H]
      })


  // TODO refactor
  private def handleCloseConnection(
    deviceId: DeviceId,
    ref:      Ref[H, Map[DeviceId, Topic[H, WebSocketFrame]]]
  ): H[Option[String]] =
    loggerH.debug(s"Handle connection close for device: ${deviceId.value}") >>
      ref.get
        .flatMap { subscriptionMap =>
          loggerH.debug(s"Subscription ref size: ${subscriptionMap.size}") >>
            (subscriptionMap.get(deviceId) match {
              case Some(topic) =>
                topic.close.flatMap[Unit] {
                  case Right(_) =>
                    ref.tryUpdate(_ - deviceId).flatMap {
                      case true =>
                        loggerH.debug(
                          s"Successfully remove from subscription ref and close the topic for device: ${deviceId.value}"
                        )
                      case false =>
                        loggerH.error(s"Can't remove topic from subscription ref. Investigation needed")
                    }
                  case Left(error) =>
                    loggerH.error(s"Error during close device topic: ${deviceId.value}. Error: $error")
                }

              case None =>
                loggerH.warn(s"Can't find topic for device: ${deviceId.value}")
            })
        }
        .as(NoMessage)

  private def getDevice(deviceId: String): H[Option[DeviceEntity]] =
    U.unlift(deviceService.getDevice(DeviceId(deviceId)))
      .flatMap {
        case Right(value) => value.some.pure[H]
        case Left(error) =>
          loggerH.error(s"Error during getting device: $deviceId. Error: $error").as(None)
      }
}

object WalkerWSApi {
  val NoMessage: Option[String] = Option.empty[String]
}
