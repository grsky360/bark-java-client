package ilio.bark

import io.kotest.matchers.shouldBe

class EndpointsTest : TestBase({

    it("verify endpoints") {
        Endpoints.health shouldBe "/healthz"
        Endpoints.info shouldBe "/info"
        Endpoints.push shouldBe "/push"
        Endpoints.ping shouldBe "/ping"
    }

})
