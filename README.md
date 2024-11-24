# **Alleysway Android Application**

### A modern and intuitive Android application designed to enhance user interaction with municipal services.

---

## **Table of Contents**
1. [Overview](#overview)
2. [Features](#features)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Usage](#usage)
6. [Technologies Used](#technologies-used)
7. [Project Structure](#project-structure)

---

## **Overview**
Alleysway is a dynamic Android application aimed at simplifying the user experience with municipal services. The app enables users to:
- Report issues in their local area.
- Track the status of service requests.
- View local events and announcements.

The app leverages a user-friendly interface combined with robust data handling capabilities using Firebase and binary search trees.

---

## **Features**
- **Issue Reporting**: Users can log and report local issues with detailed descriptions.
- **Service Request Tracking**: Track the status of service requests (Pending, In Progress, Completed).
- **Local Events**: View upcoming community events and announcements.
- **Binary Search Tree Integration**: Efficient handling and sorting of service requests for seamless data retrieval.
- **Search and Update Requests**: Quickly find and update the status of existing service requests.

---

## **Installation**

### Prerequisites
- Android Studio (latest version recommended).
- JDK 11 or higher.
- Firebase project set up for backend support.

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/alleysway.git
   cd alleysway
   ```
2. Open the project in Android Studio:
   - Navigate to `File > Open`.
   - Select the project's root directory.

3. Sync Gradle:
   - Android Studio will prompt you to sync Gradle files; click "Sync Now".

4. Build the project:
   - Go to `Build > Build Bundle(s) / APK(s)` and generate the APK for testing.

---

## **Configuration**

### Firebase Integration
1. Set up a Firebase project:
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create a new project or use an existing one.

2. Add the `google-services.json` file:
   - Download the `google-services.json` file for your Firebase project.
   - Place it in the `app/` directory of the project.

3. Configure the Firebase Realtime Database rules:
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }

---

## **Usage**

### Run the App
1. Connect your Android device or start an emulator.
2. Run the app from Android Studio:
   - Click the green play button or go to `Run > Run 'app'`.

### Key Functionalities
- **Log Issues**:
  - Navigate to the "Report Issue" section.
  - Enter issue details and submit.

- **Track Service Requests**:
  - Access the "Service Requests" section to view all requests.
  - Search for a request by its ID and update its status.

- **View Local Events**:
  - Open the "Local Events" tab to browse upcoming announcements.

---

## **Technologies Used**
- **Languages**: Kotlin, Java
- **Frameworks**: Android SDK
- **Backend**: Firebase Realtime Database
- **Tools**: Android Studio, Gradle
- **Algorithms**: Binary Search Tree for efficient data management.

---

## **Project Structure**

### Key Files and Folders
- **`app/src/main/java`**: Contains the source code.
  - `ServiceRequestStatusForm.cs`: Handles the form logic for tracking requests.
  - `TreeNode.cs`: Defines the Binary Search Tree implementation.
  - `ServiceRequest.cs`: Represents a single service request.

- **`google-services.json`**: Firebase configuration file.

- **`README.md`**: Documentation explaining Alleysway app.

---

