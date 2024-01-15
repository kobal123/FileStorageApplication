# File store application
This project aims to create an application similar to DropBox or Google drive.

The main reasons for why I started this project:
- Learn more about HTTP status codes, methods, etc..
- Goof around with HTMX
- Learn more Vanilla Javascript
- Try out Google cloud / AWS
- Try to pay more attention to writing tests / automated testing.
- Making smaller commits to github, but it seems like it's not going great.

# Authentication

For the authentication I used Spring security by itself. Later I found [Keycloak](https://www.keycloak.org/) and since it looked
interesting I started integrating it into the application. It is possible to turn it off using the application.properties file.
