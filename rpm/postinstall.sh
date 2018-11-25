mkdir -p /opt/web-image-search-engine/
touch /tmp/spring.log
/usr/bin/getent group webimagesearchengine || /usr/sbin/groupadd -r webimagesearchengine
/usr/bin/getent passwd webimagesearchengine || /usr/sbin/useradd -r -d /opt/web-image-search-engine/ -s /sbin/nologin -g webimagesearchengine webimagesearchengine
/usr/bin/chown -R webimagesearchengine:webimagesearchengine /opt/web-image-search-engine/
/usr/bin/chown -R webimagesearchengine:webimagesearchengine /tmp/spring.log