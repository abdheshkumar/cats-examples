package abtechsoft
import io.circe.Encoder
import io.circe.Decoder
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric._
import eu.timepit.refined.string._
import eu.timepit.refined.boolean._
import eu.timepit.refined.collection._
import io.circe.refined._
object Validations {
  type LanguageCode = String Refined MatchesRegex[W.`"^[a-z]{2}$"`.T]
  type ProductName = String Refined NonEmpty

}

final case class Translation(
    lang: Validations.LanguageCode,
    name: Validations.ProductName
)
object Translation {
  implicit val decode: Decoder[Translation] =
    Decoder.forProduct2("lang", "name")(Translation.apply)

  implicit val encode: Encoder[Translation] =
    Encoder.forProduct2("lang", "name")(t => (t.lang, t.name))
}
