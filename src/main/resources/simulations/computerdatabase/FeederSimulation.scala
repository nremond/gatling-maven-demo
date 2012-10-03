package computerdatabase

import akka.util.duration._
import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._

class FeederSimulation extends Simulation {

	val computerFeeder = new com.excilys.ebi.gatling.core.feeder.Feeder {

		import java.util.UUID
		import org.joda.time.DateTime
		import org.joda.time.format.DateTimeFormat
		import org.apache.commons.math3.random.{ RandomData, RandomDataImpl }

		private val randomData: RandomData = new RandomDataImpl
		private val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

		def hasNext = true

		def next: Map[String, String] = {
			val introduceDate = new DateTime(randomData.nextInt(1960, 2000), randomData.nextInt(1, 12), 1, 0, 0, 0, 000)
			val discontinuedDate = introduceDate.plusYears(randomData.nextInt(1, 10))

			Map("company" -> "12", 
				"introduced" -> dateTimeFormat.print(introduceDate), 
				"discontinued" -> dateTimeFormat.print(discontinuedDate), 
				"name" -> ("SuperComputer v_" + UUID.randomUUID))
		}
	}


	def apply = {

		val baseURL = "http://computer-database.herokuapp.com"

		val httpConf = httpConfig
						.baseURL(baseURL)

		val scn 
			= scenario("Play with the Computer Database")
				.feed(computerFeeder)
				.exec(
					http("Index page")
						.get("/")
						.check(
							css("head title").is("Computers database"),
							currentLocation.is(baseURL + "/computers")
						)
				)

				.pauseExp(3 seconds)
				.exec(
					http("Register a computer")
						.post("/computers")
						.param("company", "${company}")
						.param("introduced", "${introduced}")
						.param("discontinued", "${discontinued}")
						.param("name", "${name}")
						.check(
							css("div.alert-message strong").is("Done!")
						)
				)

		List(scn.configure.users(100).ramp(20).protocolConfig(httpConf))
	}
}
