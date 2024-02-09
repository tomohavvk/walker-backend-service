package org.tomohavvk.walker

import fs2.kafka.KafkaConsumer

case class EventConsumer[F[_], K, E](topic: String, consumer: KafkaConsumer[F, K, E])
