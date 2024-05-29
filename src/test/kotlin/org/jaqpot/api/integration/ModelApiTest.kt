package org.jaqpot.api.integration

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.*
import org.jaqpot.api.model.ModelDto
import org.junit.jupiter.api.Test
import java.util.*


class ModelApiTest : AbstractIntegrationTest() {

    @Test
    fun testGetModelsEmpty() {
        given().contentType(ContentType.JSON)
            .`when`()
            .get("/v1/models")
            .then()
            .statusCode(200)
            .body("", equalTo(Collections.emptyList<ModelDto>()))
    }

    @Test
    fun testPostModelWithoutLibrariesAndFeatures() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "jaqpotpyVersion": "1.0.0",
                    "libraries": [],
                    "dependentFeatures": [],
                    "independentFeatures": [],
                    "meta": {
                        "id": 3,
                        "mpampis": "hello"
                    },
                    "public": true,
                    "type": "some type",
                    "reliability": 5,
                    "pretrained": false,
                    "actualModel": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="
                }
            """.trimIndent()
            ).post("/v1/models")
            .then()
            .statusCode(201)
            .header("Location", matchesRegex(".*/v1/models/[0-1]+$"))
            .body(`is`(emptyString()))

        given().contentType(ContentType.JSON)
            .`when`()
            .get("/v1/models")
            .then()
            .assertThat()
            .statusCode(200)
            .body(".", hasSize<ModelDto>(1));
    }

    fun testPostModelWithLibrariesAndFeatures() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "jaqpotpyVersion": "1.0.0",
                    "libraries": [
                        {
                            "name": "sklearn",
                            "version": "0.20.1"
                        }
                    ],
                    "dependentFeatures": [{
                        "name": "Y",
                        "featureType": "DEPENDENT"
                    }],
                    "independentFeatures": [{
                        "name": "dose",
                        "featureType": "INDEPENDENT"
                    },
                    {
                        "name": "liability",
                        "featureType": "INDEPENDENT"
                    }],
                    "meta": {
                        "id": 3,
                        "mpampis": "hello"
                    },
                    "public": true,
                    "type": "some type",
                    "reliability": 5,
                    "pretrained": false,
                    "actualModel": "ewoJImluZm8iOiB7CgkJIl9wb3N0bWFuX2lkIjogIjU0MTM0N2Y1LTMyZDYtNDNkYy1iZjc5LTA4ODVkYzYzZWYyMCIsCgkJIm5hbWUiOiAiamFxcG90IGFwaSB2MiIsCgkJInNjaGVtYSI6ICJodHRwczovL3NjaGVtYS5nZXRwb3N0bWFuLmNvbS9qc29uL2NvbGxlY3Rpb24vdjIuMS4wL2NvbGxlY3Rpb24uanNvbiIsCgkJIl9leHBvcnRlcl9pZCI6ICIzNDUwMzcyNyIKCX0sCgkiaXRlbSI6IFsKCQl7CgkJCSJuYW1lIjogIm1vZGVsIiwKCQkJIml0ZW0iOiBbCgkJCQl7CgkJCQkJIm5hbWUiOiAiZ2V0QWxsIiwKCQkJCQkicmVxdWVzdCI6IHsKCQkJCQkJImF1dGgiOiB7CgkJCQkJCQkidHlwZSI6ICJub2F1dGgiCgkJCQkJCX0sCgkJCQkJCSJtZXRob2QiOiAiR0VUIiwKCQkJCQkJImhlYWRlciI6IFtdLAoJCQkJCQkidXJsIjogewoJCQkJCQkJInJhdyI6ICJ7e2Jhc2VVcmx9fS9tb2RlbHMvIiwKCQkJCQkJCSJob3N0IjogWwoJCQkJCQkJCSJ7e2Jhc2VVcmx9fSIKCQkJCQkJCV0sCgkJCQkJCQkicGF0aCI6IFsKCQkJCQkJCQkibW9kZWxzIiwKCQkJCQkJCQkiIgoJCQkJCQkJXQoJCQkJCQl9CgkJCQkJfSwKCQkJCQkicmVzcG9uc2UiOiBbXQoJCQkJfQoJCQldCgkJfSwKCQl7CgkJCSJuYW1lIjogImluZmVyZW5jZSIsCgkJCSJpdGVtIjogWwoJCQkJewoJCQkJCSJuYW1lIjogIk5ldyBGb2xkZXIiLAoJCQkJCSJpdGVtIjogW10KCQkJCX0KCQkJXQoJCX0sCgkJewoJCQkibmFtZSI6ICJyb290IiwKCQkJInJlcXVlc3QiOiB7CgkJCQkiYXV0aCI6IHsKCQkJCQkidHlwZSI6ICJiZWFyZXIiLAoJCQkJCSJiZWFyZXIiOiBbCgkJCQkJCXsKCQkJCQkJCSJrZXkiOiAidG9rZW4iLAoJCQkJCQkJInZhbHVlIjogInt7dG9rZW59fSIsCgkJCQkJCQkidHlwZSI6ICJzdHJpbmciCgkJCQkJCX0KCQkJCQldCgkJCQl9LAoJCQkJIm1ldGhvZCI6ICJHRVQiLAoJCQkJImhlYWRlciI6IFtdLAoJCQkJInVybCI6IHsKCQkJCQkicmF3IjogInt7YmFzZVVybH19LyIsCgkJCQkJImhvc3QiOiBbCgkJCQkJCSJ7e2Jhc2VVcmx9fSIKCQkJCQldLAoJCQkJCSJwYXRoIjogWwoJCQkJCQkiIgoJCQkJCV0KCQkJCX0KCQkJfSwKCQkJInJlc3BvbnNlIjogW10KCQl9LAoJCXsKCQkJIm5hbWUiOiAiY3JlYXRlIiwKCQkJInJlcXVlc3QiOiB7CgkJCQkiYXV0aCI6IHsKCQkJCQkidHlwZSI6ICJiZWFyZXIiLAoJCQkJCSJiZWFyZXIiOiBbCgkJCQkJCXsKCQkJCQkJCSJrZXkiOiAidG9rZW4iLAoJCQkJCQkJInZhbHVlIjogInt7dG9rZW59fSIsCgkJCQkJCQkidHlwZSI6ICJzdHJpbmciCgkJCQkJCX0KCQkJCQldCgkJCQl9LAoJCQkJIm1ldGhvZCI6ICJQT1NUIiwKCQkJCSJoZWFkZXIiOiBbXSwKCQkJCSJib2R5IjogewoJCQkJCSJtb2RlIjogInJhdyIsCgkJCQkJInJhdyI6ICJ7XG4gICAgXCJwdWJsaWNcIjogdHJ1ZVxufSIsCgkJCQkJIm9wdGlvbnMiOiB7CgkJCQkJCSJyYXciOiB7CgkJCQkJCQkibGFuZ3VhZ2UiOiAianNvbiIKCQkJCQkJfQoJCQkJCX0KCQkJCX0sCgkJCQkidXJsIjogewoJCQkJCSJyYXciOiAie3tiYXNlVXJsfX0vbW9kZWxzIiwKCQkJCQkiaG9zdCI6IFsKCQkJCQkJInt7YmFzZVVybH19IgoJCQkJCV0sCgkJCQkJInBhdGgiOiBbCgkJCQkJCSJtb2RlbHMiCgkJCQkJXQoJCQkJfQoJCQl9LAoJCQkicmVzcG9uc2UiOiBbXQoJCX0sCgkJewoJCQkibmFtZSI6ICJrZXljbG9hayB0b2tlbiIsCgkJCSJyZXF1ZXN0IjogewoJCQkJIm1ldGhvZCI6ICJQT1NUIiwKCQkJCSJoZWFkZXIiOiBbXSwKCQkJCSJib2R5IjogewoJCQkJCSJtb2RlIjogInVybGVuY29kZWQiLAoJCQkJCSJ1cmxlbmNvZGVkIjogWwoJCQkJCQl7CgkJCQkJCQkia2V5IjogImNsaWVudF9pZCIsCgkJCQkJCQkidmFsdWUiOiAiYWRtaW4tY2xpIiwKCQkJCQkJCSJ0eXBlIjogInRleHQiCgkJCQkJCX0sCgkJCQkJCXsKCQkJCQkJCSJrZXkiOiAiY2xpZW50X3NlY3JldCIsCgkJCQkJCQkidmFsdWUiOiAiNjJyOFFtNzlnand0QTZ3Rk1ZdDQxSW5VSzcxOG53ekoiLAoJCQkJCQkJInR5cGUiOiAidGV4dCIKCQkJCQkJfSwKCQkJCQkJewoJCQkJCQkJImtleSI6ICJncmFudF90eXBlIiwKCQkJCQkJCSJ2YWx1ZSI6ICJjbGllbnRfY3JlZGVudGlhbHMiLAoJCQkJCQkJInR5cGUiOiAidGV4dCIKCQkJCQkJfSwKCQkJCQkJewoJCQkJCQkJImtleSI6ICJyZWFsbSIsCgkJCQkJCQkidmFsdWUiOiAiIiwKCQkJCQkJCSJ0eXBlIjogInRleHQiLAoJCQkJCQkJImRpc2FibGVkIjogdHJ1ZQoJCQkJCQl9CgkJCQkJXQoJCQkJfSwKCQkJCSJ1cmwiOiB7CgkJCQkJInJhdyI6ICJodHRwOi8vbG9jYWxob3N0OjgwNzAvcmVhbG1zL2phcXBvdC1sb2NhbC9wcm90b2NvbC9vcGVuaWQtY29ubmVjdC90b2tlbiIsCgkJCQkJInByb3RvY29sIjogImh0dHAiLAoJCQkJCSJob3N0IjogWwoJCQkJCQkibG9jYWxob3N0IgoJCQkJCV0sCgkJCQkJInBvcnQiOiAiODA3MCIsCgkJCQkJInBhdGgiOiBbCgkJCQkJCSJyZWFsbXMiLAoJCQkJCQkiamFxcG90LWxvY2FsIiwKCQkJCQkJInByb3RvY29sIiwKCQkJCQkJIm9wZW5pZC1jb25uZWN0IiwKCQkJCQkJInRva2VuIgoJCQkJCV0KCQkJCX0KCQkJfSwKCQkJInJlc3BvbnNlIjogW10KCQl9CgldLAoJImV2ZW50IjogWwoJCXsKCQkJImxpc3RlbiI6ICJwcmVyZXF1ZXN0IiwKCQkJInNjcmlwdCI6IHsKCQkJCSJ0eXBlIjogInRleHQvamF2YXNjcmlwdCIsCgkJCQkicGFja2FnZXMiOiB7fSwKCQkJCSJleGVjIjogWwoJCQkJCSIiCgkJCQldCgkJCX0KCQl9LAoJCXsKCQkJImxpc3RlbiI6ICJ0ZXN0IiwKCQkJInNjcmlwdCI6IHsKCQkJCSJ0eXBlIjogInRleHQvamF2YXNjcmlwdCIsCgkJCQkicGFja2FnZXMiOiB7fSwKCQkJCSJleGVjIjogWwoJCQkJCSIiCgkJCQldCgkJCX0KCQl9CgldCn0="
            }
            """.trimIndent()
            ).post("/v1/models")
            .then()
            .statusCode(201)
            .header("Location", matchesRegex(".*/v1/models/[0-1]+$"))
            .body(`is`(emptyString()))

        given().contentType(ContentType.JSON)
            .`when`()
            .get("/v1/models")
            .then()
            .assertThat()
            .statusCode(200)
            .body("$.libraries", hasSize<ModelDto>(1))
            .body("$.independentFeatures", hasSize<ModelDto>(2))
            .body("$.dependentFeatures", hasSize<ModelDto>(1));
    }

}
