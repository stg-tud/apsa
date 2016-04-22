FROM ubuntu:14.04
MAINTAINER Ben Hermann <hermann@st.informatik.tu-darmstadt.de>

ENV LANG=en_US.UTF-8 \
    LLVM_VERSION=3.6

RUN apt-get update

RUN apt-get -y --no-install-recommends install \
        clang-${LLVM_VERSION} \
        llvm-${LLVM_VERSION} \
        llvm-${LLVM_VERSION}-dev \
        llvm-${LLVM_VERSION}-runtime \
        llvm \
        libcap-dev \
        git \
        wget \
        unzip \
        binutils \
        vim \
        joe

RUN useradd -m llvm && \
    echo llvm:llvm | chpasswd && \
    cp /etc/sudoers /etc/sudoers.bak && \
    echo 'llvm  ALL=(root) NOPASSWD: ALL' >> /etc/sudoers
USER llvm
WORKDIR /home/llvm

RUN sudo update-alternatives --install /usr/bin/clang clang /usr/bin/clang-${LLVM_VERSION} 360
RUN sudo update-alternatives --install /usr/bin/clang++ clang++ /usr/bin/clang++-${LLVM_VERSION} 360
RUN sudo update-alternatives --install /usr/bin/opt opt /usr/bin/opt-${LLVM_VERSION} 360

ADD examples examples
ADD exercises exercises

RUN sudo chown -R llvm examples
RUN sudo chown -R llvm exercises