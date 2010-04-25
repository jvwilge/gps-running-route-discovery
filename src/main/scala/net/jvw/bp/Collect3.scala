package net.jvw.bp;

import java.io._
import scala.io.Source
import scala.collection.mutable._

object Collect3 {

def main(args: Array[String]) {
	if (args.length<5) {
		println("geef 5 argumenten mee, 1 is bronbestand, 2 is bestandsnaam voor de output, 3 is minPts, 4 is radius en 5 is radius van refPoint")
		return
	}

	val inputFileName=args(0)
	val outputFileName=args(1)
	val fw=new FileWriter(outputFileName)
	val minPts=args(2).toInt //minimum points within radius to be a core point
	val radius=args(3).toDouble
	val refRadius=args(4).toDouble

	val start=System.nanoTime

	fw.write ("routeId,id,x,y,soort, i1, i2\n")

	val all=CsvUtil.collectFromCsv(inputFileName)

	println (all.length + " points found")

	val yellowRadius=0.0002
	val redRadius=0.0003
	
	all.foreach { t=>
		val inRadius=all.filter(_._1.isInRadius(t._1, yellowRadius))
		val inRadius2=all.filter(_._1.isInRadius(t._1, 0.0005))
		val s=inRadius.size
		val p=t._1
		if (s==1 | s==2 ) {
			fw.write(p.getCsvString() + ", eindpunt, " + s + ", " + inRadius2.size +"\n")
		} else 	if (s==3) {
			fw.write(p.getCsvString() + ", tussenpunt, " + s + ", " + inRadius2.size +"\n")
		} else if (s>3) {
			fw.write(p.getCsvString() + ", knooppunt, " + s + ", " + inRadius2.size +"\n")
		}
		
	}
	
	//huidge punt niet nodig zit al in !redRadius

	fw.close()

	val stop=System.nanoTime
	println("time elapsed: " + (stop-start)/1000000  + "ms")
}



}