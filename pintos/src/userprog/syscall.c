#include "userprog/syscall.h"
#include <stdio.h>
#include <syscall-nr.h>
#include "threads/interrupt.h"
#include "threads/thread.h"
#include "userprog/process.h"
#include "devices/shutdown.h"
#include "threads/vaddr.h"
#include "threads/synch.h"
#include "threads/malloc.h"
#include "userprog/pagedir.h"
#include "filesys/filesys.h"

typedef void system_handler_func (uint32_t*, struct intr_frame*);

system_handler_func system_exit;
system_handler_func system_write;
system_handler_func system_null;
system_handler_func system_halt;
system_handler_func system_exec;
system_handler_func system_wait;
system_handler_func system_create;
system_handler_func system_remove;


system_handler_func *syscall_functions_table[SYS_LEN];

static void syscall_handler (struct intr_frame *);

void
syscall_init (void) 
{
  intr_register_int (0x30, 3, INTR_ON, syscall_handler, "syscall");
  syscall_functions_table[SYS_EXIT]  = system_exit;
  syscall_functions_table[SYS_WRITE] = system_write;
  syscall_functions_table[SYS_NULL]  = system_null;
  syscall_functions_table[SYS_HALT]  = system_halt;
  syscall_functions_table[SYS_EXEC]  = system_exec;
  syscall_functions_table[SYS_WAIT]  = system_wait;
  //not implemented yet

  syscall_functions_table[SYS_CREATE]   = system_create;
  syscall_functions_table[SYS_REMOVE]   = system_remove;
  syscall_functions_table[SYS_OPEN]   = system_exit;
  syscall_functions_table[SYS_FILESIZE] = system_exit;
  syscall_functions_table[SYS_READ]   = system_exit;
  syscall_functions_table[SYS_SEEK]   = system_exit;
  syscall_functions_table[SYS_TELL]   = system_exit;
  syscall_functions_table[SYS_CLOSE]    = system_exit;
}

void check_valid_addr(uint32_t* addr, uint32_t* args, struct intr_frame* f) {

  if (!is_user_vaddr (addr) || !pagedir_get_page (thread_current ()->pagedir, addr)) {
    args[1] = -1;
    system_exit (args, f);
  }
}


static void
syscall_handler (struct intr_frame *f UNUSED) 
{
  
  // printf("System call number: %d\n", args[0]);
  // Check if args is in user's address space and that it is mapped before dereferencing it
  if (!is_user_vaddr(f->esp) || !pagedir_get_page(thread_current ()->pagedir,f->esp)) {
    int input[2] = {0,-1};
    system_exit(input,f);
  }
  uint32_t* args = ((uint32_t*) f->esp);
  syscall_functions_table[args[0]](args, f);
}

struct semaphore exec_sema;

void system_exec (uint32_t* args, struct intr_frame* f) {
  check_valid_addr(args[1], args, f);
  sema_init(&exec_sema,0);
  f->eax = process_execute((char*)args[1]);
}

void system_wait (uint32_t* args, struct intr_frame* f) {
    f->eax = process_wait(args[1]);
}


void system_exit (uint32_t* args, struct intr_frame* f) {
  struct thread *cur_thread = thread_current();
  struct wait_status *cur_state = cur_thread->state;
  lock_acquire(&cur_state->lock);
  cur_state->exit_code = args[1]; //save exit code
  printf("%s: exit(%d)\n",cur_thread->name,cur_state->exit_code);
  sema_up(&(cur_state->dead));
  cur_state->status--;
  lock_release(&cur_state->lock);
  if (cur_state->status == 0) { //parent dead, child alive
    free(cur_state);
  }
  
  //for every children, decrement status and free if status == 0
  struct list_elem *elem;
  struct wait_status *cur_child_wait_status;
  if (list_empty(&(cur_thread->children))) thread_exit();
  elem = list_begin(&(cur_thread->children));
  while (elem != list_end(&(cur_thread->children))) {
    
    cur_child_wait_status = list_entry(elem,struct wait_status, child);
    elem = list_next(elem);
    lock_acquire(&cur_child_wait_status->lock);
    cur_child_wait_status->status--;
    lock_release(&cur_child_wait_status->lock);
    if (cur_child_wait_status->status == 0) { //parent dead, child alive
      list_remove(list_prev(&elem));
      free(cur_child_wait_status);
    }
  }
  thread_exit();
}

void system_write (uint32_t* args, struct intr_frame* f) {
  printf("%s",args[2]); 
}

void system_null (uint32_t* args, struct intr_frame* f) {
  f->eax = args[1] + 1;
}

void system_halt (uint32_t* args UNUSED, struct intr_frame* f UNUSED) {
  shutdown_power_off();
}

void system_create (uint32_t* args, struct intr_frame* f) {
  check_valid_addr (args[1], args, f);
  char *file_name = (char *)args[1];
  if (!file_name) {
    args[1] = -1;
    system_exit (args, f);
  }
  f->eax = filesys_create (file_name, (off_t)args[2]);
}

void system_remove (uint32_t* args, struct intr_frame* f) {
  check_valid_addr (args[1], args, f);
  char *file_name = (char *)args[1];
  f->eax = filesys_remove(file_name);
}