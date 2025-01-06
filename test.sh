#!/bin/bash



vtune -report hotspots \
      -r Data/runE\
      --search-dir sym=/usr/lib/debug \
      --search-dir src=/usr/src/debug \
      --search-dir sym=/home/hb478/repos/graal-instrumentation/sdk/mxbuild/linux-amd64/GRAALVM_LIBGRAAL_JAVA21/graalvm-libgraal-openjdk-21.0.2+13.1/lib \
      --search-dir sym=/usr/lib/debug/lib/modules/$(uname -r) \
      -source-search-dir /home/hb478/repos/graal-instrumentation/sdk/mxbuild/linux-amd64/GRAALVM_LIBGRAAL_JAVA21 \
      -source-search-dir /home/hb478/repos/are-we-fast-yet/benchmarks/Java/src \
      -source-object function=java\:\:util\:\:Arrays\:\:setAll \
      -group-by=basic-block,address \
      -column=block,"CPU Time:Self",assembly
