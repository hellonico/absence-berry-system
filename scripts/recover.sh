
# docker run -it -v `pwd`/db:/usr/src/app/db -e date=$1 absence-recover
lein run -m absence.recover $1
