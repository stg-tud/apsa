FROM ubuntu:14.04
MAINTAINER Ben Hermann <hermann@st.informatik.tu-darmstadt.de>

ENV LANG=en_US.UTF-8 \
    LLVM_VERSION=3.6

RUN apt-get update

# Get all the binaries we may need
RUN apt-get -y --no-install-recommends install \
        clang-${LLVM_VERSION} \
        llvm-${LLVM_VERSION} \
        llvm-${LLVM_VERSION}-dev \
        llvm-${LLVM_VERSION}-runtime \
        libcap-dev \
        git \
        wget \
        unzip \
        xz-utils \
        binutils \
        vim \
        joe \
        cmake \
        make 

# Get build dependencies for LLVM
RUN apt-get -y --no-install-recommends build-dep llvm-toolchain-3.6

RUN useradd -m llvm && \
    echo llvm:llvm | chpasswd && \
    cp /etc/sudoers /etc/sudoers.bak && \
    echo 'llvm  ALL=(root) NOPASSWD: ALL' >> /etc/sudoers
USER llvm
WORKDIR /home/llvm

RUN sudo update-alternatives --install /usr/bin/clang clang /usr/bin/clang-${LLVM_VERSION} 360
RUN sudo update-alternatives --install /usr/bin/clang++ clang++ /usr/bin/clang++-${LLVM_VERSION} 360
RUN sudo update-alternatives --install /usr/bin/opt opt /usr/bin/opt-${LLVM_VERSION} 360
RUN sudo update-alternatives --install /usr/bin/opt opt /usr/bin/opt-${LLVM_VERSION} 360
RUN sudo update-alternatives --install /usr/bin/python python /usr/bin/python3 360

RUN wget http://llvm.org/releases/3.8.0/llvm-3.8.0.src.tar.xz && \
    tar xvf llvm-3.8.0.src.tar.xz && \
    cd llvm-3.8.0.src && \
    cp Makefile.config.in Makefile.config && \
    mkdir build && \
    cd build && \
    cmake -g “Unix Makefiles” ../ && \
    make



ADD examples examples
ADD exercises exercises

RUN sudo chown -R llvm examples
RUN sudo chown -R llvm exercises