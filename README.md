# Spring Boot Todo REST API demo.

This project is an attempt to create a good example of how to make a traditional 
"todo" app using a REST API. This API would be suitable for building a mobile or
web app frontend.

When run locally, Swagger documentaion for all the available endpoints can be 
found at:

http://localhost:8080/swagger-ui/index.html.

# Usage

this API allows you to create an account and then add tasks to that account. tasks
have a description, a title, a start and end date, as well as a checked value which
is null if the task is not complete, and a date of completion if the task is finished

the API also allows you to create projects, which have all the same features of a
task but act as a container - one project may contain many subtasks.

The API also allows you to call the OPENAI completion API to offer you encouragement
to complete a task, or congratulate you for doing so. It will do this in whatever
personality you specify.


# Setup
## Application settings

to get this app to work you will need to create an application.properties files in
main/java/resources containing the below values

    database settings
    spring.datasource.url= {database connection URL}
    spring.datasource.username= {your username here}
    spring.datasource.password= {your password here}
    spring.jpa.properties.hibernate.dialect= org.hibernate.dialect.MySQL5InnoDBDialect
    spring.jpa.hibernate.ddl-auto= update
    
    JWT settings
    lazy.app.jwtSecret= {your JWT secret here}
    lazy.app.jwtExpirationMs= {JWT expiration time (milliseconds)}
    lazy.app.resetExpirationMs={password reset token expiration time (milliseconds)}
    
    Mail configuration
    spring.mail.host= {email hostname here}
    spring.mail.port= {port no}
    spring.mail.username= {email username}
    spring.mail.password= {email password}
    spring.mail.auth=plain
    spring.mail.starttls.enable=true

    OpenAPI configuration
    openai.model= {GPT model required}
    openai.api.url=https://api.openai.com/v1/chat/completions
    openai.api.key= {your api key here}

The API has a rate limiter which is configured in application.yml

You can change its values declaratively by altering the capacity, time and units
fields there.

## Database Setup
To properly configure security, the database will need roles to assign to users and 
hibernate will not do this automatically, so the following queries will need to be
run against the database on setup:
    
    INSERT INTO roles(name) VALUES('ROLE_USER');
    INSERT INTO roles(name) VALUES('ROLE_MODERATOR');
    INSERT INTO roles(name) VALUES('ROLE_ADMIN');

Additionally, the tests expect some values in their database in order to pass.
you can either run them on the main database (not recommended), or connect them to
a test database, by creating a new application.properties file in 
test/java/resources. either way, the following commands are required to ensure the
database chosen has the appropriate content:

### user setup
    INSERT INTO `{your schema name here}`.`users` (`id`, `email`, `password`, `username`) VALUES ('1', 'test@test1.com', 'asdsad', 'testUser1');
    INSERT INTO `{your schema name here}`.`users` (`id`, `email`, `password`, `username`) VALUES ('2', 'test@test2.com', 'asdsad', 'testUser2');

### Tasks setup
    INSERT INTO `{your schema name here}`.`tasks` (`id`, `description`, `title`) VALUES ('1', 'testDescription1', 'testTitle1');
    INSERT INTO `{your schema name here}`.`tasks` (`id`, `description`, `title`) VALUES ('2', 'testDescription2', 'testTitle2');
    INSERT INTO `{your schema name here}`.`tasks` (`id`, `description`, `title`) VALUES ('3', 'testDescription3', 'testTitle3');
    INSERT INTO `{your schema name here}`.`tasks` (`id`, `description`, `title`) VALUES ('4', 'testDescription4', 'testTitle4');

### Projects setup
    INSERT INTO `{your schema name here}`.`projects` (`id`, `description`,`title`, `owner_id`) VALUES ('1', 'testDescription1', 'testTitle1', '1');

### Assign tasks to users
    INSERT INTO `{your schema name here}`.`user_tasks` (`user_id`, `task_id`) VALUES ('1', '1');
    INSERT INTO `{your schema name here}`.`user_tasks` (`user_id`, `task_id`) VALUES ('1', '2');
    INSERT INTO `{your schema name here}`.`user_tasks` (`user_id`, `task_id`) VALUES ('1', '3');
    INSERT INTO `{your schema name here}`.`user_tasks` (`user_id`, `task_id`) VALUES ('1', '4');


## Docker
The dockerfile at the root directory of this project should allow for this program
to be containerised, you simply need to navigate to the root directory and run

     docker build -t{container name}:{tag} .

And then run via 
    
    docker run -p {exposed port}:8080
 

