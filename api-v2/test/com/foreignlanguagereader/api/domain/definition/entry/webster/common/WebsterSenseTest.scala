package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.Json

class WebsterSenseTest extends AnyFunSpec {
  describe("a variant") {
    val webster =
      "{\"sn\":\"1 a\",\"dt\":[[\"text\",\"{bc}to suddenly break open or come away from something often with a short, loud noise \"],[\"wsgram\",\"no obj\"],[\"vis\",[{\"t\":\"The balloon {it}popped{/it}. [={it}burst{/it}]\"},{\"t\":\"We heard the sound of corks {it}popping{/it} as the celebration began.\"},{\"t\":\"One of the buttons {it}popped{/it} off my sweater.\"}]],[\"wsgram\",\"+ obj\"],[\"vis\",[{\"t\":\"Don't {it}pop{/it} that balloon!\"},{\"t\":\"She {it}popped{/it} the cork on the champagne. [=she opened the bottle of champagne by removing the cork]\"}]]]}"
    val domain =
      "{\"definingText\":{\"text\":[\"{bc}to suddenly break open or come away from something often with a short, loud noise \"],\"examples\":[{\"text\":\"The balloon {it}popped{/it}. [={it}burst{/it}]\"},{\"text\":\"We heard the sound of corks {it}popping{/it} as the celebration began.\"},{\"text\":\"One of the buttons {it}popped{/it} off my sweater.\"},{\"text\":\"Don't {it}pop{/it} that balloon!\"},{\"text\":\"She {it}popped{/it} the cork on the champagne. [=she opened the bottle of champagne by removing the cork]\"}]}}"

    it("can be read from JSON") {
      val variants = Json
        .parse(webster)
        .validate[WebsterSense]
        .get
    }

    it("can be written back out to JSON") {
      val input = Json
        .parse(webster)
        .validate[WebsterSense]
        .get
      val output = Json.toJson(input).toString()
      assert(output == domain)
    }
  }
}
