# Alibaba Cloud Image Search Demo

## Introduction

This demo workshop showcases the [Image Search API](https://www.alibabacloud.com/help/product/66413.htm), an Alibaba Cloud
service to search products by uploading a photo.

The demo is a website compatible with desktop computers and smartphones. It contains two main pages:
* A management page where a user can creates/updates/deletes products in the database.
* A search page that lets users to find products that look similar to an uploaded photo. The picture can come from a
  JPEG file or directly from a camera when the user views this page on a smartphone.

## Change the default category

By default Image Search use **88888888** category which is **OTHERS**.
If you plan to use other category you have to manually recompile it and change the code.

[Link to the categories](https://www.alibabacloud.com/help/doc-detail/66623.html)

In order to change the default category you need to edit one file called [ImageSearchServiceImpl.java](https://github.com/wojciehm/Alibaba-Cloud-Image-Search-Demo/blob/master/src/main/java/com/alibaba/intl/imagesearch/services/impl/ImageSearchServiceImpl.java) at line 95.

from `request.setCatId(category.getId());`

to
`request.setCatId(category.DIGITAL_DEVICES.getId());`

and line 162
from
`// request.setCatId(ObjectCategory.FURNITURE.getId()); `

to
`request.setCatId(ObjectCategory.DIGITAL_DEVICES.getId());`

or any needed category ID.

|Category ID|Description|
|--- |--- |
|0|TOPS|
|1|DRESSES|
|2|BOTTOMS|
|3|BAGS|
|4|SHOES|
|5|ACCESSORIES|
|6|SNACKS|
|7|MAKEUP|
|8|BOTTLE_DRINKS|
|9|FURNITURE|
|20|TOYS|
|21|UNDERWEARS|
|22|DIGITAL_DEVICES|
|88888888|OTHERS|

### Compile the code on macOS

I am using macOS Mojave and brew to install maven. The installation is very simple and works out of the box.

1. Install brew on macOS [Brew on macOS](https://brew.sh/)
2. Install maven using brew:
```shell
brew update
brew install maven      
brew cleanup  
```
3. Install rpm using brew
```shell
brew install rpm
```
4. Once installed you can proceed with the build.

## Build
Clone the project, open a terminal and type:

    mvn clean install

This generates the fat JAR "target/web-image-search-engine.jar".

If you plan to install this demo on a RPM-based Linux distribution, you can type the following command:

    mvn rpm:rpm

This generates the package "target/rpm/web-image-search-engine/RPMS/noarch/web-image-search-engine-*.noarch.rpm".

The RPM contains the fat JAR and a Systemd script. It allows users to easily execute the server when the machine
starts. It also automatically restarts the application after a crash.

## Installation and execution
If you just want to run the fat JAR, open a terminal and run:

    java -jar web-image-search-engine-latest.jar

This starts the server on the port 8080 and outputs the logs in the console.

### RPM based Linux distribution Installation

If you are using a RPM-based Linux distribution, such as CentOS 7.4+, you can install the RPM with the following
commands:

    yum -y update
    yum -y install web-image-search-engine-latest.noarch.rpm

You can start the server with the following command:

    systemctl start web-image-search-engine.service

You can check the logs by running the following command:

    journalctl --unit=web-image-search-engine

The server should start and listen to the port 8080. If you prefer to use the port 80, you can setup a reverse proxy
such as [Nginx](https://www.linode.com/docs/development/java/how-to-deploy-spring-boot-applications-nginx-ubuntu-16-04/#reverse-proxy).

If you want to automatically run the server when the machine starts, enter the following command:

    systemctl enable web-image-search-engine.service

## Demo configuration

### Image Search instance creation

Before running the application, you first need to create a "Product Search instance":
* Go to [https://www.alibabacloud.com/](https://www.alibabacloud.com/), login (or create an account) and go to the
  web console.
* Obtain an accessKeyId and an accessKeySecret (in the web console, click on your user picture on the top-right of the
  page and select "AccessKey").
* Create an [Product Search instance](https://www.alibabacloud.com/help/doc-detail/66569.htm) (search type = "Product
  Search").
* If you want, create an [ECS instance](https://www.alibabacloud.com/help/product/25365.htm) with CentOS 7.4.

Open your web browser on the following URL: http://your_server_address:8080/

### Image Search demo configuration

If this is the first time you use the application, you will be redirected to the configuration page.

1.


Once configured, using this web application is pretty straightforward:
1. Go to the "Manage objects" page: it displays all the sample products registered in the database. You can add, modify
   and delete objects.
2. Go back to the "Home" page and upload an image. If have added enough objects, you should always see 20 results with
   various scores.

After many searches you will find that the results are not always meaningful. For example you will get 20 results even
if you search with a picture of an object that doesn't exist in the database (in fact, the value "20" is hardcoded in
the demo source code). This is a limitation of this API, and there is no simple solution. The absolute value of the
score doesn't help (unless you search with the exact same picture as a registered object), only its relative value
can be used for sorting results.

## Pre-compiled rpms:
- [TOPS](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-tops.noarch.rpm)
- [DRESSES](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-dresses.noarch.rpm)
- [BOTTOMS](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-bottoms.noarch.rpm)
- [BAGS](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-bags.noarch.rpm)
- [SHOES](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-bags.noarch.rpm)
- [ACCESSORIES](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-accesssories.noarch.rpm)
- [SNACKS](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-snacks.noarch.rpm)
- [MAKEUP](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-makup.noarch.rpm)
- [BOTTLE_DRINKS](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-bottle-drinks.noarch.rpm)
- [FURNITURE](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-furniture.noarch.rpm)
- [TOYS](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-toys.noarch.rpm)
- [UNDERWEARS](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-underwears.noarch.rpm)
- [DIGITAL_DEVICES](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-digital-devices.noarch.rpm)
- [OTHERS](https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-others.noarch.rpm)

## Ready to copy&paste CentOS commands to execute install

#### TOPS
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-tops.noarch.rpm
yum -y install web-image-search-tops.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### DRESSES
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-dresses.noarch.rpm
yum -y install web-image-search-dresses.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### BOTTOMS
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-bottoms.noarch.rpm
yum -y install web-image-search-bottoms.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### BAGS
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-bags.noarch.rpm
yum -y install web-image-search-bags.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### SHOES
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-shoes.noarch.rpm
yum -y install web-image-search-shoes.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### ACCESSORIES
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-accessories.noarch.rpm
yum -y install web-image-search-accessories.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### SNACKS
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-snacks.noarch.rpm
yum -y install web-image-search-snacks.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### MAKEUP
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-makeup.noarch.rpm
yum -y install web-image-search-makeup.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### BOTTLE_DRINKS
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-bottle-drinks.noarch.rpm
yum -y install web-image-search-bottle-drinks.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### FURNITURE
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-furniture.noarch.rpm
yum -y install web-image-search-furniture.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### TOYS
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-engine-toys.noarch.rpm
yum -y install web-image-search-engine-toys.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### UNDERWEARS
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-engine-underwears.noarch.rpm
yum -y install web-image-search-engine-underwears.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### DIGITAL_DEVICES
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-engine-digital-devices.noarch.rpm
yum -y install web-image-search-engine-digital-devices.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```
#### OTHERS
```
yum -y update
wget https://imagesearchrpm.oss-ap-southeast-1.aliyuncs.com/web-image-search-engine-others.noarch.rpm
yum -y install web-image-search-engine-others.noarch.rpm
systemctl start web-image-search-engine.service
systemctl enable web-image-search-engine.service
```

There are few solutions for this problem:
* Have a huge database of images, like [Taobao](https://www.taobao.com/).
* Teach the Image Search API to recognize bad results. <br>For example if the customer only sells furniture, then
  non-furniture objects should also be registered in the database and marked as "BAD", like this the program can filter
  the Image Search results by removing the BAD objects.

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
