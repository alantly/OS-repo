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
#include "filesys/file.h"
#include <list.h>

typedef void system_handler_func (uint32_t*, struct intr_frame*);

struct file_descriptor* find_fd(int fd);



int fd_counter = 2;

system_handler_func system_exit;
system_handler_func system_write;
system_handler_func system_null;
system_handler_func system_halt;
system_handler_func system_exec;
system_handler_func system_wait;
system_handler_func system_create;
system_handler_func system_remove;
system_handler_func system_open;
system_handler_func system_close;
system_handler_func system_filesize;
system_handler_func system_seek;
system_handler_func system_tell;
system_handler_func system_read;

system_handler_func *syscall_functions_table[SYS_LEN];

static void syscall_handler (struct intr_frame *);

struct file_descriptor {
  int fd;
  struct file *f;
  struct list_elem list_elem;
};

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
  syscall_functions_table[SYS_OPEN]  = system_open;
  syscall_functions_table[SYS_CLOSE] = system_close;
  syscall_functions_table[SYS_CREATE]   = system_create;
  syscall_functions_table[SYS_REMOVE]   = system_remove;
  syscall_functions_table[SYS_FILESIZE] = system_filesize;
  syscall_functions_table[SYS_SEEK]   = system_seek;
  syscall_functions_table[SYS_TELL]   = system_tell;
  syscall_functions_table[SYS_READ]   = system_read;
  
  //not implemented yet
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


void system_exec (uint32_t* args, struct intr_frame* f) {
  check_valid_addr(args[1], args, f);
  f->eax = process_execute((char*)args[1]);
}

void system_wait (uint32_t* args, struct intr_frame* f) {
  f->eax = process_wait(args[1]);
}


void system_exit (uint32_t* args, struct intr_frame* f) {
  if (!is_user_vaddr(f->esp + sizeof(args[1]))) {
    args[1] = -1;
  }
  struct thread *cur_thread = thread_current();
  struct wait_status *cur_state = cur_thread->state;
  lock_acquire(&cur_state->lock);
  cur_state->exit_code = args[1]; //save exit code

  //printf("%s: exit(%d)\n",cur_thread->name,cur_state->exit_code);
  /*
  sema_up(&(cur_state->dead));
  cur_state->status--;
  */
  lock_release(&cur_state->lock);
  if (cur_state->status == 0) { //parent dead, child alive
    free(cur_state);
  }
  
  //for every children, decrement status and free if status == 0
  /*
  struct list_elem *elem;
  struct wait_status *cur_child_wait_status;
  if (list_empty(&(cur_thread->children))) {
    thread_exit();
  }
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
  */
  thread_exit();
}

void system_write (uint32_t* args, struct intr_frame* f) {
  int fd = args[1];
  char *buf = (char*) args[2];
  uint32_t size = args[3];
  check_valid_addr(buf, args, f);
  sema_down(&fs_sema);

  if (fd == 1 && size > 0) {
    if ('\0' == NULL) {
      //printf("____\nHELLYA\n_____\n");
    }
    putbuf(buf,size);
    f->eax = size;
  } else {
    struct file_descriptor* cur_file_descriptor = find_fd(fd);
    if (cur_file_descriptor && is_writeable(cur_file_descriptor->f) == 0) {
      f -> eax =file_write (cur_file_descriptor -> f, buf, size);
    } else {
      f -> eax = 0;
    }
  }
  sema_up(&fs_sema);
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

void system_filesize (uint32_t* args, struct intr_frame* f) {
  int fd = args[1];
  sema_down(&fs_sema);
  struct file_descriptor * new_fd = find_fd(fd);
  if (new_fd == NULL) {
    f->eax = 0;
  } else {
    f->eax = file_length(new_fd->f);
  }
  sema_up(&fs_sema);
}

void system_seek (uint32_t* args, struct intr_frame* f) {
  int fd  = args[1];
  uint32_t pos = args[2];
  sema_down(&fs_sema);
  struct file_descriptor* cur_fd = find_fd(fd);
  if (fd) {
    file_seek (cur_fd -> f, pos);
  }
  sema_up(&fs_sema);
}

void system_tell (uint32_t* args, struct intr_frame* f) {
  int fd  = args[1];
  sema_down(&fs_sema);
  struct file_descriptor* cur_fd = find_fd(fd);
  f->eax = 0;
  if (fd) {
    f->eax = file_tell (cur_fd -> f);
  }
  sema_up(&fs_sema);
}

void system_open (uint32_t* args, struct intr_frame* f) {
  
  check_valid_addr (args[1], args, f);
  /*
  if (!is_user_vaddr(f->esp + sizeof(args[1]))) {
    int input[2] = {0, -1};
    system_exit (input, f);
  }
  */
  sema_down(&fs_sema);
  char *file_name = (char *)args[1];
  struct file* opened_file = filesys_open(file_name);
  if (!opened_file) {
    f->eax = -1; 
  } else {
    struct file_descriptor *new_file_descriptor = malloc(sizeof (struct file_descriptor));
    if (!new_file_descriptor) {
      file_close(opened_file);
      f->eax = -1;
    } else {
      struct thread* cur_thread = thread_current(); 
      list_push_back (&(cur_thread -> file_list), &(new_file_descriptor -> list_elem));
      // Setting deny_write of the file to true to avoid executable of a running process from being modified
      if (strcmp (cur_thread->name, file_name) == 0)
        file_deny_write (opened_file);
      new_file_descriptor -> f  = opened_file;
      new_file_descriptor -> fd = fd_counter++;
      f -> eax = new_file_descriptor -> fd;
    }
  }
  sema_up(&fs_sema);
}

void system_close (uint32_t* args, struct intr_frame* f) {
  int fd = args[1];
  if (fd < 2) {
    return;
  }
  struct list_elem *e;
  struct thread* cur_thread = thread_current();

  struct file_descriptor *cur_file_descriptor = find_fd(fd);
  if (cur_file_descriptor) {
    file_close (cur_file_descriptor -> f);
    list_remove (&(cur_file_descriptor -> list_elem));
    free (cur_file_descriptor);
  }
}

void system_read (uint32_t* args, struct intr_frame* f) {
  int fd = args[1];
  char *buf = (char*) args[2];
  uint32_t size = args[3];
  check_valid_addr(buf, args, f);
  sema_down(&fs_sema);

  if (fd == 0 && size > 0) {
    buf[0] = input_getc();
    f -> eax = 1;
  } else {
    struct file_descriptor* cur_file_descriptor = find_fd(fd);
    if (cur_file_descriptor) {
      f -> eax =file_read (cur_file_descriptor -> f, buf, size);
    } else {
      f -> eax = -1;
    }
  }
  sema_up(&fs_sema);
}

struct file_descriptor* find_fd(int fd) {
  struct list_elem *e;
  struct thread* cur_thread = thread_current();
  struct file_descriptor * cur_file_descriptor = NULL;

  for (e = list_begin(&(cur_thread -> file_list)); e != list_end (&(cur_thread -> file_list));
       e = list_next (e)) {
      cur_file_descriptor = list_entry(e, struct file_descriptor, list_elem);
      if (cur_file_descriptor -> fd == fd) {
        return cur_file_descriptor;
      }    
    }
  return cur_file_descriptor;
}
