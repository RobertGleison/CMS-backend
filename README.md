# Netflix Plus

A video streaming platform offering secure content delivery and management capabilities.

## Overview

Netflix Plus is a comprehensive streaming solution split across three repositories:

1. **User Platform** 
   The main streaming application where users can watch content.  
   [View Users Repository](https://github.com/RobertGleison/content-management-system-users)

3. **Content Management System** 
   Administrative interface for content managers.  
   [View Server Repository](https://github.com/RobertGleison/content-management-system-admins)

5. **Backend Server** (Current Repository)  
   Core backend services and API.  
   

## System Architecture

### Infrastructure Components

* **Database Layer**
  * Apache Cassandra instance hosted on Google Cloud Platform (GCP)
  * Optimized for high-throughput content metadata management
  
* **Backend Services**
  * Deployed on GCP Virtual Machine
  * RESTful API endpoints for content delivery and management
  * Token-based authentication system
  
* **Security Layer**
  * Firebase Authentication integration
  * NGINX reverse proxy for load balancing and security
  * Secured API endpoints with token verification

### Content Delivery System

#### Streaming Implementation
* HTTP Live Streaming (HLS) protocol for adaptive bitrate streaming
* Content segmentation into chunks for efficient delivery
* NGINX-powered authenticated access to video segments

#### Content Processing
* FFMPEG integration for video transcoding and format optimization
* Chunked upload/download mechanism for large files
* GCP Cloud Storage integration for scalable content hosting

## Security Features

* End-to-end authentication for all service interactions
* Secure content delivery through authenticated HLS streams
* Role-based access control between user and admin platforms

## Technical Stack
* **Frontend**: Java, Android Studio
* **Backend**: Java, Spring Boot
* **Database**: Apache Cassandra
* **Cloud Provider**: Google Cloud Platform
* **Additional Tools**:
  * FFMPEG for video processing
  * NGINX for reverse proxy and content delivery
  * Firebase for authentication
  * HLS for adaptive streaming
  * 
### Backend architecture
![Screenshot from 2024-12-21 11-44-25](https://github.com/user-attachments/assets/ba1f7216-acda-4a1b-a4fe-36b67247c87f)
![Screenshot from 2024-12-21 11-42-57](https://github.com/user-attachments/assets/c4718212-7b9f-42e9-b3d1-8dae51a7edca)
![Screenshot from 2024-12-21 11-42-42](https://github.com/user-attachments/assets/b79eb042-df28-4a55-ae49-714b9682372c)
