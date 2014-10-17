#include "userprog/syscall.h"
#include <stdio.h>
#include <syscall-nr.h>
#include "threads/interrupt.h"
#include "threads/thread.h"

typedef void system_handler_func (uint32_t*, struct intr_frame*);

system_handler_func system_exit;
system_handler_func system_write;
system_handler_func system_null;

system_handler_func *syscall_functions_table[SYS_LEN];

static void syscall_handler (struct intr_frame *);

void
syscall_init (void) 
{
  intr_register_int (0x30, 3, INTR_ON, syscall_handler, "syscall");
  syscall_functions_table[SYS_EXIT]  = system_exit;
  syscall_functions_table[SYS_WRITE] = system_write;
  syscall_functions_table[SYS_NULL]  = system_null;

  //not implemented yet
  syscall_functions_table[SYS_HALT] 	= system_exit;
  syscall_functions_table[SYS_EXEC]  	= system_exit;
  syscall_functions_table[SYS_WAIT]  	= system_exit;
  syscall_functions_table[SYS_CREATE]  	= system_exit;
  syscall_functions_table[SYS_REMOVE]  	= system_exit;
  syscall_functions_table[SYS_OPEN]  	= system_exit;
  syscall_functions_table[SYS_FILESIZE] = system_exit;
  syscall_functions_table[SYS_READ]  	= system_exit;
  syscall_functions_table[SYS_SEEK]  	= system_exit;
  syscall_functions_table[SYS_TELL]  	= system_exit;
  syscall_functions_table[SYS_CLOSE]  	= system_exit;
}


static void
syscall_handler (struct intr_frame *f UNUSED) 
{
  uint32_t* args = ((uint32_t*) f->esp);
  // printf("System call number: %d\n", args[0]);
  syscall_functions_table[args[0]](args, f);
}

void system_exit (uint32_t* args, struct intr_frame* f) {
	f->eax = args[1];
    thread_exit();
}

void system_write (uint32_t* args, struct intr_frame* f) {
	printf("%s\n",args[2]);	
}

void system_null (uint32_t* args, struct intr_frame* f) {
	f->eax = args[1] + 1;
}
