# Task Management System — Java TCP Client–Server
This is a basic multi-client Java CLI Task Management System built with Java TCP sockets, developed by a four people of team. Users can register, log in, and manage their personal tasks. The server supports concurrent clients using multithreading, and all data is stored in simple text files.

---

## Features

* TCP client–server communication
* **REGISTER / LOGIN** authentication
* Add, view, and delete tasks
* File-based storage for each user
* Multi-threaded server
* Basic reminder system

---

## Project Structure

src/
 └── taskManager/
      ├── TCPServerPool.java
      ├── TCPClient.java
      ├── TaskManagementProtocol.java
      ├── TaskDataManager.java
      └── TaskCategory.java
data/
 └── <username>.txt
 
---

## Commands

The client communicates with the server using simple **single-line text commands**.
Each command is parsed by the server and returns a text-based response.

---

### User Authentication

#### 1- REGISTER
```
REGISTER <username> <password>
```
* Creates a new user account
* Fails if the username already exists

**Example
```
REGISTER john doe123
```

#### 2- LOGIN
```
LOGIN <username> <password>
```
* Authenticates an existing user
* Must be called before any task-related command
* Invalid credentials return an error

**Example:
```
LOGIN john doe123
```
---

ADD <category> <date> <description> – Add a new task
VIEW – Display all tasks
DELETE <taskId> – Delete a task by ID

#### 3- ADD TASK

```
ADD <category> <YYYY-MM-DD> <description>
```
* Adds a new task for the logged-in user
* Categories must match an enum value (WORK, PERSONAL, STUDY, OTHER)
* The description can contain spaces
**Example:
```
ADD_TASK WORK 2026-01-15 Finish assignment
```
---
#### 4- VIEW TASKS

```
VIEW
```
* Returns all tasks belonging to the logged-in user
* Each task includes:
  * Task ID
  * Category
  * Due date
  * Description
**Example Response:
```
1 | WORK | 2026-01-15 | Finish Java assignment
2 | PERSONAL | 2026-01-20 | Go to school
```
---

#### 5- DELETE TASK

```
DELETE <taskId>
```

* Removes a task by its ID
* Returns an error if the ID does not exist
**Example:
```
DELETE 2
```
---

## Limitations
Passwords stored in plain text
No database 
Basic validation only

## Possible Improvements
- Password hashing
- MySQL storage
---------------------------------------------------------------------------------
This is the Network Programming team project assignment from Vilnius University Šiauliai Academy.
