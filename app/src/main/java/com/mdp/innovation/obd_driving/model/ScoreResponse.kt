package com.mdp.innovation.obd_driving.model

data class ScoreResponse(val token: String) {

    data class Args(val test: String)
    data class Headers(val host: String,
                       val accept: String,
                       val `accept-encoding`: String,
                       val `accept-language`: String,
                       val `cache-control`: String,
                       val cookie: String,
                       val dnt: String,
                       val `postman-token`: String,
                       val `user-agent`: String,
                       val `x-forwarded-port`: String,
                       val `x-forwarded-proto`: String)

}