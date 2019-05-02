package de.tud.stg.br

import java.io._

// This is just a small demo class to demonstrate exception handling in Bytecode/Three-address code.
class IO {


    def readPair(fileName : String): Int = {
        var b1, b2 = 0
        try {
            val fin = new FileInputStream(new File(fileName));
            b1 = fin.read()
            b2 = fin.read()
            fin.close();
            b1/b2;
        } catch {
            case ioe : IOException =>
                log(ioe.getMessage)
                throw ioe
            case e : ArithmeticException =>
                log(s"$b2 is 0")
                throw e
        }
    }

    def log (msg : String) : Unit = {}

}
