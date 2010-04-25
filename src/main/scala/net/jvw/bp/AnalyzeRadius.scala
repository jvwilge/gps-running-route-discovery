package net.jvw.bp;

import java.io._
import scala.io.Source
import scala.collection.mutable._

/**
 * Inlezen csv en punten binnen radius vinden
 * Bij veel rode punten (bv atletiekbaan is de radius 0.000125 (8.571 meter))
 * TODO, is rood wel rood, volgens mij blauw! 7 feb
 * dan is geel 0.0002 (13.714 meter)
 */
object AnalyzeRadius {


def main(args: Array[String]) {
	if (args.length<5) {
		println("geef 5 argumenten mee, 1 is bronbestand, 2 is bestandsnaam voor de output, 3 is minPts, 4 is radius en 5 is radius van refPoint")
		return
	}

	val inputFileName=args(0)
	val outputFileName=args(1)
	val fw=new FileWriter(outputFileName)
	val minPts=args(2).toInt //minimum points within radius to be a core point
	//val radius=0.000025
	val radius=args(3).toDouble
	val refRadius=args(4).toDouble

	val start=System.nanoTime

	fw.write ("routeId,id,x,y,inRadius\n")

	val all=collectFromCsv(inputFileName)

	println (all.length + " points found")
	val refPoint=new GpsPoint("ref",0,5.151432,52.069355)
	val filtered=all.filter(_.isInRadius(refPoint, refRadius))
 
	println (filtered.length + " points found after filter")

	val points=new ArrayBuffer[(GpsPoint, Int)] //int is inRadius

	filtered.foreach { p:GpsPoint =>
		val inRadius=filtered.filter(_.isInRadius(p, radius))
		//TODO is sorteren nog wel nodig? 10% snelheidsverbetering!
	//	val sorted=inRadius.sortWith(_.distanceTo(p) < _.distanceTo(p))
		
		points+=((p, inRadius.length))
	
		fw.write(p.getCsvString() + ", " + inRadius.length + "\n")
	}

	fw.close()
	
	val stop=System.nanoTime
	println("time elapsed: " + (stop-start)/1000000  + "ms")
}

def writeOutputCsv(buf:ArrayBuffer[GpsPoint]){
	
}

def collectFromCsv(fileLocation:String):ArrayBuffer[GpsPoint] = {
	val source=Source.fromFile(new File(fileLocation))
	val ab=new ArrayBuffer[GpsPoint]()
	val lines=source.getLines
	lines.foreach {
	    line:String=>
		if (!line.startsWith("routeId,id,")){
			ab+=getPoint(line)
		}
	}
	ab
}

def getPoint(line:String) = {
	val l=line.split(", ")
	new GpsPoint(l(0), l(1).toInt, l(2).toDouble, l(3).toDouble)
}

}