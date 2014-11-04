#include "threads/interrupt.h"

#ifndef USERPROG_SYSCALL_H
#define USERPROG_SYSCALL_H

void syscall_init (void);
void invalid_func(struct intr_frame *);

struct semaphore fs_sema;

#endif /* userprog/syscall.h */
