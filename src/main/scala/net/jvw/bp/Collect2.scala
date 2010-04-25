package net.jvw.bp;

import java.io._
import scala.io.Source
import scala.collection.mutable._

/**
 * Inlezen csv en punten binnen radius vinden
 * Bij veel rode punten (bv atletiekbaan is de radius 0.000125 (8.571 meter))
 * dan is geel 0.0002 (13.714 meter)
 */
object Collect2 {


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
	val refPoint=new GpsPoint("refPoint",0,5.15,52.078, true)
	val filtered=all
   .filter(_._1.isInRadius(refPoint, refRadius))
 
	println (filtered.length + " points found after filter")

	val points=filtered

	val yellowRadius=0.00005
	val redRadius=0.00004
	
	
	var currentPoint=refPoint

		
	//huidge punt niet nodig zit al in !redRadius

	for (it<-1 to 5){
		
		val inRadius=all.filter(_._1.isInRadius(currentPoint, yellowRadius)).filter(!_._1.isInRadius(currentPoint, redRadius))
							.filter(t=> !t._1.connectedTo.contains(currentPoint))

	val yellow=all.filter(_._1.isInRadius(currentPoint, yellowRadius))
	val red   =all.filter(_._1.isInRadius(currentPoint, redRadius))	
	val f1=all.filter(t=> t._1.connectedTo.contains(currentPoint))
	
	println ("-")
		println("currentPoint: " + currentPoint)
		yellow.foreach {
			 t=>
				println ("--yellow: " + t._1)
				t._1.connectedTo.foreach { s=>
					println ("  " + s)
				}
		}
		red.foreach {
			 t=>
				println ("--red: " + t._1)
				t._1.connectedTo.foreach { s=>
					println ("  " + s)
				}
		}
		f1.foreach {
			 t=>
				println ("--f1: " + t._1)
				t._1.connectedTo.foreach { s=>
					println ("  " + s)
				}
		}
		println("-")
							
		if (inRadius.size == 1 ) {
			val nextPoint=inRadius(0)._1
			nextPoint.addConnection(currentPoint)
			currentPoint.addConnection(nextPoint)
			currentPoint=nextPoint
		} else if (inRadius.size == 0 ){
			//eindpunt
			println ("eindpunt")
		} else {
			//knooppunt (let op bij knoopunt, sorteren, opschonen, sorteren, opschonen, etc.)
			println("knooppunt")
		}
	} 
	
	//connecties toevoegen
	
	//bij meerdere puntens stoppen, geen punten wissen, alleen huidige knooppunt toevoegen (en daar wel rode radius wissen)


	// zoek legs met de punten uit de lijst tot een knooppunt of eindpunt
	// loop alle knooppunten af om te zien of er nieuwe legs uit te halen zijn
	
/*	
			.foreach { t=>
				currentPoint.addConnection(t._1)
				t._1.addConnection(currentPoint)
				println ("  filter: " + t._1)
				//previousPoint=currentPoint
			}
			//als niets gevonden, zoek punt binnen yellow
	}
	*/
	
	all.foreach { t=>
		println ("all: " + t._1)
		t._1.connectedTo.foreach { s=>
			println ("  " + s)
		}
	}
	
	
	
	
	val pathPoints=new ArrayBuffer[(GpsPoint, Int, Double)]
	val corePoints=new ArrayBuffer[(GpsPoint, Int, Double)]
	
	
	
	//val sorted=points.sortWith(_._2 > _._2)
	//val (p, i, r)=sorted(0)
	//corePoints+=sorted(0)
	/*
	corePoints+=((refPoint, 4, 0))
	points.filter(_._1.isInRadius(refPoint, redRadius)).foreach {
		points-=_
	}
	*/

	val stop=System.nanoTime
	println("time elapsed: " + (stop-start)/1000000  + "ms")
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