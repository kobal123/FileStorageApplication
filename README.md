# File store application
This project aims to create an application similar to DropBox or Google drive.

The main reasons for why I started this project:
- Learn more about HTTP status codes, methods, etc..
- Goof around with HTMX
- Learn more Vanilla Javascript
- Try out Google cloud / AWS
- Try to pay more attention to writing tests / automated testing.
- Making smaller commits to GitHub, but it seems like it's not going great.

# Authentication

For the authentication I used Spring security by itself. Later I found [Keycloak](https://www.keycloak.org/) and since it looked
interesting I started integrating it into the application. It is possible to turn it off using the application.properties file.

# Design considerations

For accessing directories I wanted to copy what DropBox does, meaning when accessing a file clearly shows the path of the file, like in a file system. Eg: www.filestore.com/home/directory1/directory2. Google drive uses a hash or something similar, so it is hard to tell what directroy a URL points to.

# Todo
- The front end is still in a very early stage.
- There are no tests for Google cloud.
- No AWS yet

