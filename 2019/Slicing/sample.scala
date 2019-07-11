def foo(input: boolean) -> Unit {
  val test =  !input
  var a: Int
  var b: Int
  if (test) {
    a = 2
    b = 3	
  } else {
    a = 10
    b = 20
  }
  b = 30
  println(a)
  println(b)
}


def foo(input: boolean) -> Unit {
  val test =  !input
  var a: Int
  if (test) {
    a = 2   
  } else {
    a = 10    
  }
  println(a)  
}


def foo(input: boolean) -> Unit {
  var b: Int 
  b = 30  
  println(b)
}



new Object()


NEW Object
DUP
SPECIALINVOKE java/lang/Object.init()




def bar() {
  var x = 1
  var y = 2
  x = add(x,y)
  y = add(x,y)
  val z = add(x,y)
  x = add(x,z)
  println(z)
}

def add(x: Int, y: Int):Int = 
  x + y