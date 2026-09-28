
# Secure Multithreaded Client-Server Chat Application

A robust, multithreaded chat application developed in Java, engineered with security-first design principles. This project provides concurrent client communication backed by cryptographic encryption, secure credential handling, and immutable audit logging.

## Key Technical Features

* **Multithreaded Server Architecture:** Utilizes Java socket programming to handle concurrent client connections dynamically, ensuring stable performance across multiple simultaneous active users.
* **AES Base64 Message Encryption:** Secures data in transit by encrypting chat payloads using AES, protecting message confidentiality from network-level snooping.
* **Secure User Authentication:** Implements account login workflows utilizing secure password hashing and cryptographic salting to prevent raw credential exposure.
* **Brute-Force Intrusion Prevention:** Integrates automated account lockout handling to temporarily disable accounts following repeated failed authentication attempts.
* **Synchronized CSV Security Logging:** Maintains a thread-safe, synchronized CSV security audit trail to log connection events, authentication attempts, and server errors accurately.

## Built With

* **Language:** Java
* **Core Concepts:** Sockets, Multithreading, Cryptography API, File I/O (CSV Synchronization)

## Getting Started

1. Clone the repository:
   ```bash
   git clone [https://github.com/toby07ald-sys/Secure-Chat-Server-Public.git](https://github.com/toby07ald-sys/Secure-Chat-Server-Public.git)