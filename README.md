# Image Search Workshop

## Summary
0. [Introduction](#introduction)
1. [Prerequisite](#prerequisite)
2. [How to use](#how-to-use)
3. [Run as a linux deamon](#run-as-a-linux-deamon)
4. [Tips](#tips)
5. [Online demo page](#online-demo-page)
6. [Thanks](#thanks)

## Introduction

This is a demo workshop which showcases the [Image Search](https://www.alibabacloud.com/help/product/66413.htm), an Alibaba Cloud
image search service. After the construction, you can upload an image and try products search.

The demo is a website compatible with desktop computers and smartphones. It contains two main pages:
* *Management objects* page
    * where users can add/update/delete products in the database.
* *Home* page
    * where users can search products that look similar to an uploaded photo.
    * The picture can come from a JPEG file or directly from a camera when the user views this page on a smartphone.

The following diagram represents the demo architecture.
There are two databases: one for storing names & images of registered objects, and one managed by the Image Search instance (it only stores selected image features required for searches). Both databases are synchronized via object UUIDs.

![image_search_demo_architecture.svg](images/image_search_demo_architecture.svg)


## Prerequisite

### Product Search instance
Before running the application, you first need to create a *Product Search instance* as an image search API server on Alibaba Cloud.

0. Create an Alibaba cloud account

    You need an Alibaba cloud account. If you don't have any account, you can access [https://www.alibabacloud.com/](https://www.alibabacloud.com/) and create a new account.

1. Create a Product Search instance

    You need an *Product Search instance*. You can create it from [web console](https://imagesearch.console.aliyun.com/product-search). For more information, you can see the [documents](https://www.alibabacloud.com/help/doc-detail/66569.htm).

    If you want, you can also create an [ECS instance](https://www.alibabacloud.com/help/product/25365.htm).

2. Create an access key

    You need an accessKeyId and an accessKeySecret to access your Product Search instance from local. You can create an access key from [web console](https://usercenter.console.aliyun.com/#/manage/ak).
    Or you can access the page from the "AccessKey" link at the top-right profile picture on the Alibaba cloud web console.


### Local environment requirements

This application requires following environment.
* A Windows or Linux distribution
    * CentOS 7.4+ (recommended)
* Java Runtime Environment
    * JDK 8 (recommended)
    * Maven

You can check if you installed them by follows.
```
mvn -v
java -version
```

You can check what Java version you have and change it by follows.
```
# CentOS
sudo alternatives --config java

# Mac
/usr/libexec/java_home -V
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
```


## How to use

1. Set up

    Run the following commands.

    ```
    # Change directory if needed
    cd /path/to/your/directory

    # Clone the project
    git clone https://github.com/alibabacloud-howto/image-search-workshop.git

    # Install and Build the project
    mvn clean install

    # Run the project
    java -jar target/web-image-search-engine.jar
    ```

    Ofcourse, you can use IDE (e.g. IntelliJ IDEA) to import, build and run this project.
    For tips, you can use the following command just to build the project.
    ```
    mvn package -Dmaven.test.skip=true
    ```

2. Try it!

    1. Configure
        * Open your web browser and access http://localhost:8080/
        * If this is the first time you use the application, you will be redirected to the configuration page.
        * Enter your access key and "Product Search instance" details.

    2. Access the *Manage objects* page
        * You can add/update/delete products here.
        * It displays all the sample products registered in the database.

    3. Return to the *Home* page
        * You can upload an image and see the image search result here.
        * If you have added enough products, you should always see 20 results with various scores.


## Run as a linux deamon

1. Create a RPM package

    If you plan to install this demo on a RPM-based Linux distribution, you can type the following command:
    ```
    mvn rpm:rpm
    ```

    This generates the package "target/rpm/web-image-search-engine/RPMS/noarch/web-image-search-engine-*.noarch.rpm".
    The RPM contains the fat JAR and a Systemd script. It allows users to easily execute the server when the machine
    starts. It also automatically restarts the application after a crash.

    If you just want to run the fat JAR, open a terminal and run the command:
    ```
    java -jar web-image-search-engine-latest.jar
    ```

    This starts the server on the port 8080 and outputs the logs in the console.

2. Install the RPM package

    If you are using a RPM-based Linux distribution, such as CentOS 7.4+, you can install the RPM with the following commands:
    ```
    sudo yum -y update
    sudo yum -y install path/to/web-image-search-engine-latest.noarch.rpm
    ```

3. Execute the service

    You can start the server with the following command:
    ```
    sudo systemctl start web-image-search-engine.service
    ```

    You can check the logs by running the following command:
    ```
    sudo journalctl --unit=web-image-search-engine
    ```

    The server should start and listen to the port 8080. If you prefer to use the port 80, you can setup a reverse proxy
    such as [Nginx](https://www.linode.com/docs/development/java/how-to-deploy-spring-boot-applications-nginx-ubuntu-16-04/#reverse-proxy).

    If you want to automatically run the server when the machine starts, enter the following command:
    ```
    sudo systemctl enable web-image-search-engine.service
    ```


## Tips

After many searches you will find that the results are not always meaningful. For example you will get 20 results even
if you search with a picture of an object that doesn't exist in the database (in fact, the value "20" is hardcoded in
the demo source code). This is a limitation of this API, and there is no simple solution. The absolute value of the
score doesn't help (unless you search with the exact same picture as a registered object), only its relative value
can be used for sorting results.

There are few solutions for this problem:
* Have a huge database of images, like [Taobao](https://www.taobao.com/).
* Teach the Image Search API to recognize bad results: for example if the customer only sells furniture, then non-furniture objects should also be registered in the database and marked as "BAD", like this the program can filter the Image Search results by removing the BAD objects.


## Online demo page
[http://imagesearch.abcdemo.cc/](http://imagesearch.abcdemo.cc/)


## Thanks
This application uses the following libraries/frameworks/tools:
* Backend
  * [Spring Boot](https://spring.io/projects/spring-boot)
  * [H2 Database Engine](http://www.h2database.com)
  * [Hibernate](http://hibernate.org/)
  * [Apache Commons IO](https://commons.apache.org/proper/commons-io/)
  * [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)
  * [Alibaba Cloud SDK for Java](https://github.com/aliyun/aliyun-openapi-java-sdk)
* Frontend
  * [Bootstrap](https://getbootstrap.com/)
  * [jQuery](https://jquery.org/)
  * [Lodash](https://lodash.com/)
  * [Open Iconic](https://useiconic.com/open/)
  * [Draggabilly](https://draggabilly.desandro.com/)
* Backend testing
  * [Maven Surefire Plugin](http://maven.apache.org/surefire/maven-surefire-plugin/)
  * [Mockito](http://site.mockito.org/)
  * [JaCoCo](https://www.jacoco.org/)
* Build
  * [Jenkins](https://jenkins.io/)
  * [SonarQube](https://www.sonarqube.org/)
  * [RPM Maven Plugin](https://www.mojohaus.org/rpm-maven-plugin/)
