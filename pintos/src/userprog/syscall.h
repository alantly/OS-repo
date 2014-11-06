#include "threads/interrupt.h"
#include <list.h>
#include "filesys/file.h"

#ifndef USERPROG_SYSCALL_H
#define USERPROG_SYSCALL_H

void syscall_init (void);

struct semaphore fs_sema;

struct file_descriptor {
  int fd;
  struct file *f;
  struct list_elem list_elem;
};

#endif /* userprog/syscall.h */
