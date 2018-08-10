
Overrule / Intercept Mina-SSHd Permissions

Is there a way to "overrule" whatever permissions Apache's Mina-SSHd is using for my files?

I have a (Windows 7) workstation with a folder (my working copy) which I want to mount on remote hosts (my Mac, Docker, Linux build nodes) so that I can run commands (build/test/etc) without synching a 200mb file tree.
I'm most of the way there;

- I can start/stop an Apache Mina-SSHd thread on my workstation
- I can connect to the remote host via SSH and pipe the Mina port back
- I can use SSHFS to mount the folders served by Mina on the remote host

I'm stuck because I can't set permissions, things like `chmod -R +=rwx` seem to be ignored.
While SCP commands seem to work fine, and, I can mount not-Mina SSHd folders fine; I suspect that this is something of an edge case and Mina's doing

- nothing is ever executable, which coincides with Windows' lack of a POSIX "is executable" bit
- when I "look at" a file (via `cat` or `nano`) it becomes read-only on both my host and the SSHd server
	- I think that this is the big tell; the fact that the host file becomes locked after I go and read it

So ... to save me from single-step-debugging my way through whatever handlers, does anyone know of a way to "overrule" whatever permissions Apache's Mina-SSHd is using for my files?

This happens with both `NativeFileSystemFactory` and `VirtualFileSystemFactory`
