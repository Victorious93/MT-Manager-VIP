# Victorious-Manager User Manual

> **Status: skeleton.** As of milestone M0 the app only shows a placeholder screen, so there is nothing to follow step by step yet. Each section below names the milestone that builds it. Step-by-step instructions are written only after that milestone's screens exist, so the steps match what you actually see on screen. Sections marked **Planned** describe intended behaviour and may change before release.

## How to use this manual

- Start with [Getting started](#1-getting-started).
- Words in **bold** that are unfamiliar are explained in the [Glossary](#15-glossary).
- Each step begins with an action you take, for example "Tap **Copy**".
- If a step does not match your screen, this manual may be out of date for your app version.

---

## 1. Getting started

**[M1]**

### 1.1 What the app does

Victorious-Manager is a file manager. It lets you browse, copy, move, rename and delete files on your phone and on connected storage. It also opens archives, edits text, inspects APK app files, and connects to network folders.

### 1.2 Install

**Planned.** Installation steps depend on how the app is distributed (Play Store, or a file you install directly). This section will be completed once that is decided. Installing a file directly requires allowing installs from that source in Android settings.

### 1.3 Give the app access to your files

**[M1]** Android does not let apps read your files until you allow it. Victorious-Manager offers two options:

| Option | What it gives the app | When to use it |
|---|---|---|
| **All files access** | Read and change every file the app's rules allow | Needed to browse all folders |
| **Selected folders only** (**SAF**) | Access only to the folders you pick | Use if you do not want to grant all-files access |

Step-by-step instructions for each option will be added in M1.

---

## 2. The main screen

**[M1] Planned layout**

- The screen is split into two **panes**, side by side (**dual pane**). Each pane shows one folder. You can compare or move files between the two.
- Each pane has its own **tabs**, so one pane can keep several folders open.
- The top of each pane shows the current **path**, the folder you are in.
- Toolbar buttons and menus are described per feature below.

---

## 3. Browsing files

**[M1]**

- **Open a folder:** tap the folder name.
- **Go up one level:** use the back button or the path bar.
- **Open a new tab:** described in M1.
- **Sort:** sort by name, size, date or file type; choose ascending or descending. Folders are listed first by default.
- **Filter by name:** shows only items whose name contains the text you type. Filtering is not case-sensitive.
- **Show hidden files:** off by default. Hidden files are those whose name starts with a dot (see [hidden file](#15-glossary)).
- **Large folders:** long folders load in pages, so the list can keep loading as you scroll.

---

## 4. Working with files

**[M1]**

Each sub-section will give exact steps once built. The behaviour described here is the planned design.

### 4.1 Select items
Select one or more items before running an action on them.

### 4.2 Copy and move
- **Copy** leaves the originals in place and creates new copies at the destination.
- **Move** puts the items at the destination and removes the originals.
- If a file with the same name already exists at the destination, you are asked to choose (see **Conflicts** below).
- Moving between storage devices (for example phone storage to an SD card) copies first and removes the originals only after the copy finishes (see [cross-volume move](#15-glossary)).

### 4.3 Conflicts
When the destination already has an item with the same name, you choose:

- **Skip:** leave the existing item alone and do not copy this one.
- **Overwrite:** replace the existing item completely.
- **Keep both:** save the new item under a new name, such as `photo (1).jpg`.

### 4.4 Rename
Renames one item. The app refuses names that are empty, are `.` or `..`, or contain `/`.

### 4.5 Delete
**Warning: the planned design has no recycle bin.** Deleted items are removed permanently. Confirm the final delete behaviour in this section when M1 is built.

Deleting a folder deletes everything inside it. Deleting a [symbolic link](#15-glossary) removes only the link, not the item it points to.

### 4.6 New folder and new file
Creates an empty folder or file in the current folder.

### 4.7 Properties
Shows an item's name, location, size, and last-modified date (planned; the exact fields will be confirmed when built).

---

## 5. Background jobs

**[M1]**

- Copy, move and delete run as **jobs**. Long jobs continue in the background and show a notification with progress.
- Jobs run one at a time, in the order you started them.
- You can **cancel** a running or waiting job. Cancelling stops the job; files already copied stay where they are.
- A job that fails shows the reason. Failed jobs stay in the job list until you clear them.

---

## 6. Settings

**[M1 onward]**

The full settings list is finalised when each feature is built. Planned settings:

| Setting | Milestone | What it does | Default |
|---|---|---|---|
| Theme | M1 | Light, Dark, follow the system, or Dynamic colour (colours taken from your wallpaper; Android 12 or newer) | Follow the system |
| Root mode | M7 | Allows file operations that need superuser access. Off by default. See [root](#15-glossary) | Off |
| Language | M9 | Changes the app's language | System language |

Step-by-step instructions for each setting will be added when the setting is built.

---

## 7. Archives

**[M2] Planned**

An **archive** (zip, 7z, tar, gz, xz, bz2) bundles files together, usually compressed. The app will let you:

- Open an archive and browse it like a folder, without extracting everything.
- Extract selected items or the whole archive.
- Create a new archive.
- Open and create **password-protected** zip archives.
- Work with **split archives** (one archive stored as numbered parts).
- Run an **integrity test** to check an archive for corruption without extracting it.

---

## 8. Text and code editor

**[M3] Planned**

- Opens text and source files with **syntax highlighting** (colours that show keywords, strings and comments).
- Undo and redo.
- Find and replace, with optional **regex** patterns.
- Line numbers.
- Choose or detect the text [encoding](#15-glossary). Wrong encoding shows garbled characters.
- Large files (over 50 MB) open in a mode that loads only part of the file at a time. Planned behaviour.

---

## 9. Viewers

**[M4] Planned**

- **Images:** view common image formats.
- **Audio and video:** playback.
- **PDF:** view pages.
- **Hex viewer:** shows the raw bytes of any file as numbers, for inspecting binary files. See [hex viewer](#15-glossary).

---

## 10. Utilities

**[M4, M5, M6] Planned**

- **Checksum (M4):** calculate MD5, SHA-1 or SHA-256 of a file to compare it with a published value. See [checksum](#15-glossary).
- **Bookmarks and recent files (M4):** shortcuts to folders you use often, and a list of recently opened files.
- **Global search (M4):** find files by name across folders.
- **Folder compare (M6):** shows which files differ between two folders.
- **Duplicate finder (M6):** finds files with identical contents.
- **Storage analyzer (M6):** shows which folders use the most space, as a treemap.
- **Installed apps (M5):** lists installed apps, backs up their APK files, uninstalls them, and shows app details.

---

## 11. APK tools

**[M5] Planned.** An **APK** is the file Android uses to install an app. These tools work on APK files you have.

- **APK info:** the manifest, permissions requested, [signature](#15-glossary), components and size breakdown.
- **Browse APK contents** and extract resources.
- **View the decoded `AndroidManifest.xml`.**
- **Re-sign an APK** with a key you provide or generate, and **zipalign** it (see [zipalign](#15-glossary)).

**Important:** a re-signed APK has a different [signature](#15-glossary) from the original. Android treats it as a different app. It cannot update the installed original, and the original cannot update it.

---

## 12. Terminal and scripts

**[M7] Planned.** A built-in [terminal](#15-glossary) runs command-line tools inside the app. The app will also run shell scripts. Some commands need [root](#15-glossary).

---

## 13. Network locations and sharing

**[M8] Planned**

- **Connect to a network location** over FTP, SFTP, SMB or WebDAV, and browse it as if it were a folder. See the [glossary](#15-glossary) for these protocols.
- **Share files over Wi-Fi:** the app starts a local web server and shows a QR code. Other devices on the same network open the address to download files.

**Security:** only share on networks you trust. Access rules for the share server (for example whether a password is required) will be documented when M8 is built.

---

## 14. Troubleshooting

**Planned.** Common problems and fixes will be added as features are built. Expected entries:

- Folders such as `Android/data` do not show their contents: Android restricts access to these folders. See [scoped storage](#15-glossary).
- "Operation failed: already exists": the destination has an item with the same name. Use a conflict option (see 4.3).
- A copy or delete stops: check the job list for the failure reason (see 5).

---

## 15. Glossary

Terms are listed alphabetically.

**All files access.** An Android permission that lets the app see and change all files it is allowed to reach. You turn it on in Android settings for the app.

**APK.** Android Package. The file Android installs apps from. An APK contains the app's code, resources and a [signature](#signature).

**Archive.** A single file that contains other files, usually compressed. Common types are zip, 7z, tar, gz, xz and bz2.

**Checksum.** A short fingerprint computed from a file's contents. If two files have the same checksum, their contents are almost certainly identical. MD5 and SHA-1 are fast but are not safe against deliberate tampering. SHA-256 is preferred for verifying downloads.

**Conflict.** The destination already has an item with the same name as the one you are copying or moving.

**Cross-volume move.** A move between two [volumes](#volume), such as phone storage and an SD card. The app copies the files first and removes the originals only after the copy finishes.

**Dual pane.** A screen split into two independent file lists side by side.

**Dynamic colour.** On Android 12 and newer, a colour theme taken from your wallpaper.

**Encoding.** How text characters are stored as bytes. Common encodings are UTF-8 and UTF-16. Opening a file with the wrong encoding shows garbled characters.

**FTP, FTPS.** File Transfer Protocol: an older protocol for moving files over a network. Plain FTP sends data unencrypted. FTPS adds encryption.

**Foreground service.** A background task that Android keeps running while it shows a notification. The app uses one for long copies, so they continue while the screen is off.

**Hex viewer.** A view that shows a file's raw bytes as hexadecimal numbers, next to the matching text characters.

**Hidden file.** A file or folder whose name starts with a dot, such as `.nomedia`. Hidden items are not shown in lists unless you turn on "show hidden files".

**Integrity test.** A check that reads an archive's internal checksums to detect corruption, without extracting the files.

**Job.** One copy, move or delete operation that runs in the background. Jobs are listed and can be cancelled.

**Keystore.** A password-protected file that stores signing keys and certificates.

**Regex (regular expression).** A pattern language for matching text. For example, `\d+` matches one or more digits.

**Root.** Superuser access on an Android device that has been modified to allow it. Normal use does not need root. Root mode is off by default. Careless use can change or damage system files.

**SAF (Storage Access Framework).** Android's built-in folder picker. Access granted through it covers only the folders you choose.

**Scoped storage.** Android rules that limit which folders an app can access by default, especially on Android 11 and newer.

**SFTP.** File transfer over an encrypted SSH connection.

**Signature.** The cryptographic identity an APK is signed with. Android installs an update only if its signature matches the installed app's. A re-signed APK is therefore treated as a different app.

**SMB.** The Windows file-sharing protocol. Used by many network-attached storage devices.

**Split archive.** One archive divided into numbered parts. All parts are needed to extract the contents.

**Symbolic link (symlink).** A special item that points to another file or folder. By design, the app does not follow links when deleting, so deleting a link leaves its target in place.

**Syntax highlighting.** Colouring source code by role (keywords, strings, comments) to make it easier to read.

**Tab.** An extra location open within one pane, so you can switch between folders without losing your place.

**Terminal.** A text command line running inside the app.

**Volume.** A storage device or partition, such as internal storage, an SD card or a USB drive.

**WebDAV.** File access over HTTP or HTTPS, supported by many cloud and network storage services.

**zipalign.** A tool that aligns the data inside an APK so Android can read it efficiently. Release APKs are normally zipaligned.
