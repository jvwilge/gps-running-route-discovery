package net.jvw.bp;

import java.io._
import scala.io.Source
import scala.collection.mutable._

/**
 * Inlezen csv en punten binnen radius vinden
 * Bij veel rode punten (bv atletiekbaan is de radius 0.000125 (8.571 meter))
 * dan is geel 0.0002 (13.714 meter)
 */
object Doorsnede {


def main(args: Array[String]) {
	
	/* jaagpad
	def startPoint=new GpsPoint("start", 0, 5.15124, 52.08051)
	def endPoint=new GpsPoint("end", 1, 5.15231, 52.07885)
	*/
	
	/* atletiekbaan (2 banen + stuk inlooproute)
	def startPoint=new GpsPoint("start", 0, 5.15162, 52.07589)
	def endPoint=new GpsPoint("end", 1, 5.15365, 52.07570)
	*/
	
	/* atletiekbaan (1 baan) 
	def startPoint=new GpsPoint("start", 0, 5.152832, 52.074964)
	def endPoint=new GpsPoint("end", 1, 5.153019, 52.074845)
	*/

	/* ingang Amelisweerd */ 
	def startPoint=new GpsPoint("start", 0, 5.151247, 52.069554)
	def endPoint=new GpsPoint("end", 1, 5.151475, 52.069206)
	/*
	*/

	
	def zoekRadius=0.00002 //radius waarbinnen punten gezocht worden
	def inputFileName="/Users/jeroen/Documents/vu/bachelorproject/data/output/output_jeroen_garmin.csv"
	
	def stepCount=100
	def stepX=(endPoint.getLonX-startPoint.getLonX)/stepCount
	def stepY=(endPoint.getLatY-startPoint.getLatY)/stepCount
	
	val all=collectFromCsv(inputFileName)
	
	println("start: " + startPoint)
	for (it<-0 to stepCount){
		def searchBase=new GpsPoint("s"+it, 100+it, startPoint.getLonX+stepX*it, startPoint.getLatY+stepY*it)
		//println ("s"+it + ": " + searchBase)
		println ( all.filter(_._1.isInRadius(searchBase, zoekRadius)).size )
		
	}
	println("end: " +  endPoint)
	
	println ("distance: " + endPoint.distanceTo(startPoint))
	
}

//TODO deze methode is een kopie uit Collect, maar 1 keer doen!!
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

//TODO deze methode is een kopie uit Collect, maar 1 keer doen!!
def getTuple(line:String) = {
	val l=line.trim.split(", ")
	val point=new GpsPoint(l(0), l(1).toInt, l(2).toDouble, l(3).toDouble)
	val inRadius=l(4).toInt
//	val reachabilityDistance=l(5).toDouble
	(point, inRadius, 0.0)
}

}