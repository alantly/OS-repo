CS162 Operating Systems Repo

Project solutions are under branches
release/project#/checkpoint3

Pintos is a simple instructional operating system framework for the 80x86 architecture. The software supports kernel threads,
loading and running user programs, and a file system, but it implements all of these in a very simple way.
In the Pintos projects, you and your project team will strengthen its support in all three of these
areas. You will also add a virtual memory implementation. Pintos could, theoretically, run on a regular
IBM-compatible PC. Unfortunately, it is impractical to supply every CS 162 student a dedicated PC
for use with Pintos. Therefore, we will run Pintos projects in a system simulator, that is, a program
that simulates an 80x86 CPU and its peripheral devices accurately enough that unmodied operating
systems and software can run under it. In class we will use the Bochs and QEMU simulators. Pintos
has also been tested with VMware Player.

Project 1 (release/project1/checkpoint3):
Implemented priority scheduling, and priority donation in Pintos. Priority donation
is allowing higher priority threads to donate their priority temporarily to threads that hold
resources they are waiting on such as locks, semaphores, or condition variables.

Main Project 1 source code:
pintos/src/threads/thread.h,
pintos/src/threads/thread.c,
pintos/src/threads/synch.h,
pintos/src/threads/synch.c

Project 2 (release/project1/checkpoint3):
Implemented syscalls in pintos allowing pintos to run user programs.
Designed a file descriptor system to let user programs access the file system.
and let user programs to access the file system.
User Program syscalls: EXIT, HALT, EXEC, WAIT
File System syscalls: CREATE, REMOVE, OPEN, FILESIZE, WRITE, READ, SEEK, TELL, CLOSE

Main Project 2 source code:
pintos/src/threads/thread.c,
pintos/src/threads/thread.h,
pintos/src/userprog/syscall.h,
pintos/src/userprog/syscall.c,
pintos/src/userprog/process.c

Project 3 (release/project3/checkpoint3):
Implemented a reliable Key-Value storage system using a Two Phase Commit design.
The system uses Consistent Hashing to store across slaves, and XML for communication from Client
to Master to Slave.

Main Project 3 source code:
kvstore/src/kvstore/
