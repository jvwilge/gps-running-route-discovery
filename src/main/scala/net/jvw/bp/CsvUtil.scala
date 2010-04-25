package net.jvw.bp;

import java.io._
import scala.io.Source
import scala.collection.mutable._

object CsvUtil {

def collectFromCsv(fileLocation:String):ArrayBuffer[(GpsPoint, Int, Double)] = {
	val source=Source.fromFile(new File(fileLocation))
	val ab=new ArrayBuffer[(GpsPoint, Int, Double)]()
	val lines=source.getLines
	lines.foreach {
	    line:String=>
		if (!line.startsWith("routeId,id,")){
			ab+=getTuple(line)
		}
	}
	ab
}

/*
 * Tuple: GpsPoint, InRadiusCount
 */
def getTuple(line:String) = {
	val l=line.trim.split(", ")
	val point=new GpsPoint(l(0), l(1).toInt, l(2).toDouble, l(3).toDouble)
	val inRadius=if (l.size>4 ) { l(4).toInt } else { 0 }
//	val reachabilityDistance=l(5).toDouble
	(point, inRadius, 0.0)
}

}