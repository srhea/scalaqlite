# Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
# All rights reserved.
#
# See the file LICENSE included in this distribution for details.

ENV["JAVA_OPTS"] = "-Djava.library.path=target/native"
require 'buildr/scala'
repositories.remote << 'http://www.ibiblio.org/maven2'
define 'scalaqlite' do
  project.version = '0.1.0-SNAPSHOT'
  package :jar
  compile do
    system 'cp -R src/main/native target/; make -C target/native'
  end
end
