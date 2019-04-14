#include <iostream>
#include <cstdio>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <string.h>

int main(int argc, const char * argv[]) {
	enum { READ, WRITE };
	int status;
	int pipe1[2]; // first pipe connecting child & grandchild
	int pipe2[2]; // second pipe connecting grandchild & greatgrandchild
	pid_t pid;

	// Error in arugments input
	if (argc < 2) {
		perror("Error in arguments\n");
		exit (EXIT_FAILURE);
	}

	// Create pipe 1 & pipe 2
	if (pipe(pipe1) < 0 || pipe(pipe2) < 0) {
		perror("Error in creating pipe\n");
		exit (EXIT_FAILURE);
	}

	// Fork the process
	pid = fork();

	// Failed to create fork
	if (pid < 0) {
		perror("Error in creating fork\n");
		exit (EXIT_FAILURE);
	}

	// In the child process
	else if (pid == 0) {
		// Fork the process
		pid = fork();

		// Failed to create fork
		if (pid < 0) {
			perror("Error in creating fork\n");
			exit (EXIT_FAILURE);
		}

		// In the grandchild process
		else if (pid == 0) {
			// Fork the process
			pid = fork();

			// Failed to create fork
			if (pid < 0) {
				perror("Error in creating fork\n");
				exit (EXIT_FAILURE);
			}

			// In the greatgrandchild process
			else if (pid == 0) {
				// Close unused end
				close(pipe2[READ]);
				close(pipe1[WRITE]);
				close(pipe1[READ]);

				// Write to grandchild process
				dup2(pipe2[WRITE], 1);

				// Execute "ps -A"
				execlp("ps", "ps", "-A", NULL);
			}

			// In the grandchild (parent of greatgrandchild) process
			else {
				// Close unused end
				close(pipe2[WRITE]);
				close(pipe1[READ]);

				// Read from greatgrandchild process
				dup2(pipe2[READ], 0);

				// Write to child process
				dup2(pipe1[WRITE], 1);

				// Execute "grep argv[1]"
				execlp("grep", "grep", argv[1], NULL);
			}
		}

		// In the child (parent of grandchild) process
		else {
			// Closed unused end
			close(pipe2[WRITE]);
			close(pipe2[READ]);
			close(pipe1[WRITE]);

			// Read from grandchild process
			dup2(pipe1[READ], 0);

			// Execute "wc -l"
			execlp("wc", "wc", "-l", NULL);

			// Wait for child process to finish
			wait(&status);
		}
	}

	// In the parent (parent of child) process
	else {
		// Closed unused end
		close(pipe2[WRITE]);
		close(pipe2[READ]);
		close(pipe1[WRITE]);
		close(pipe1[READ]);

		// Wait for child process to finish
		wait(&status);
	}
}

