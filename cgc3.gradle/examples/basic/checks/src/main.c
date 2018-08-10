

#include <stdlib.h>

#include "shared.h"
#include <stdio.h>

int main(int argc, char* argv[])
{
  for (int i = 3; i >= 0; --i)
    printf("This is a test program ... (i repeat %d more times)\n", i);
  
  return (14 == foo(2.f)) ? EXIT_SUCCESS : EXIT_FAILURE;
}
