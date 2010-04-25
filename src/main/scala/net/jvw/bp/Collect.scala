package net.jvw.bp;

import java.io._
import scala.io.Source
import scala.collection.mutable._

/**
 * Inlezen csv en punten binnen radius vinden
 * Bij veel rode punten (bv atletiekbaan is de radius 0.000125 (8.571 meter))
 * dan is geel 0.0002 (13.714 meter)
 */
object Collect {


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

	fw.write ("routeId,id,x,y,inRadius,reachability\n")

	val all=collectFromCsv(inputFileName)

	println (all.length + " points found")
	val refPoint=new GpsPoint("ref",0,5.151432,52.069355)
	val filtered=all
   .filter(_._1.isInRadius(refPoint, refRadius))

//0.005 duurt 10.8 sec
//0.01 duurt 267 sec. (met 119K/565K punten)
//0.0012 is nodig om een goede analyse te kunnen doen
//0.12 is Utrecht en omgeving
 
 
	println (filtered.length + " points found after filter")

	val points=filtered

val yellowRadius=0.0005
//val redRadius=0.000125
val redRadius=0.0004
	val pathPoints=new ArrayBuffer[(GpsPoint, Int, Double)]
	val corePoints=new ArrayBuffer[(GpsPoint, Int, Double)]
	
	
	
	//val sorted=points.sortWith(_._2 > _._2)
	//val (p, i, r)=sorted(0)
	//corePoints+=sorted(0)
	corePoints+=((refPoint, 4, 0))
	points.filter(_._1.isInRadius(refPoint, redRadius)).foreach {
		points-=_
	}
	println("corepoint(0) aangemaakt")

	
for (it<-1 to 20){
	var itemsLeft=true
	while (itemsLeft && corePoints.length>0){
		val center=corePoints.first
		val centerPoint=center._1
		
		//TODO controleer eerst of er een corepoint/knooppunt in de buurt is
		val filtered2=points.filter(_._1.isInRadius(centerPoint, yellowRadius)).filter(!_._1.isInRadius(centerPoint, redRadius))
		val sorted2=filtered2.sortWith(_._2 > _._2)
		//println ("sorted2.length:" + sorted2.length)
		if (sorted2.length<minPts){
			sorted2.foreach {
				points-=_
			}
			itemsLeft=false
		} else {
			val (corePoint, i1, _)=sorted2(0)
			corePoints+=sorted2(0)
			points.filter(_._1.isInRadius(sorted2(0)._1, redRadius)).foreach {
				points-=_
			}
		}
	}
	if (corePoints.length>0) {
		pathPoints+=corePoints.first
		corePoints-=corePoints.first
	}
	println ("points: " + points.length + ", core points: " + corePoints.length)
}



//TODO : if met filter voor i>=minPts
var x=0
	corePoints.foreach { t =>
		val (p, i, _)=t
		if (i>=minPts) fw.write(p.getCsvString() + ", " + i + ", " + x + "\n")
		x+=1
	}
	pathPoints.foreach { t =>
		val (p, i, _)=t
		if (i>=minPts) fw.write(p.getCsvString() + ", " + i + ", " + x + "\n")
		x+=1
	}
	
	fw.close()
	
	val stop=System.nanoTime
	println("time elapsed: " + (stop-start)/1000000  + "ms")
}

def writeOutputCsv(buf:ArrayBuffer[GpsPoint]){
	
}

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

def getTuple(line:String) = {
	val l=line.split(", ")
	val point=new GpsPoint(l(0), l(1).toInt, l(2).toDouble, l(3).toDouble)
	val inRadius=l(4).toInt
	val reachabilityDistance=l(5).toDouble
	(point, inRadius, reachabilityDistance)
}

}