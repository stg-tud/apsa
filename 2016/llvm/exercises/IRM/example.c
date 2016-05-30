#include <stdlib.h>
#include <stdio.h>

/*
void irmCall(int actual, int expected) {
	if (actual == expected) {
		printf("Call to function with value %d detected. Exiting.\n", actual);
		exit(1);
	}
}
*/

int increment(int arg) {
	return (arg + 1);
}

int main(int argc, char *argv[]) {
	printf("Starting program.\n");

	int i = 0;

	while (i < 50) {
		printf("Calling with %d.\n", i);
		i = increment(i);
	}

	printf("Normal termination of program.\n");
}

