#include <stdlib.h>
#include <stdio.h>

int k(int a, int b) {
  if(a < b) {
    return a;
  } else {
    return b;
  }
}

int main(int argc, char *argv[]) {
  if( argc == 3 ) {
    int a = atoi(argv[1]);
    int b = atoi(argv[2]);
    int c = k(a, b);
    printf("%d\n", c);
  } else {
    printf("no\n");
  }

  return 0;
}
