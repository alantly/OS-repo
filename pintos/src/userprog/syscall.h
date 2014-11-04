#include "threads/interrupt.h"

#ifndef USERPROG_SYSCALL_H
#define USERPROG_SYSCALL_H

void syscall_init (void);

struct semaphore fs_sema;

#endif /* userprog/syscall.h */
