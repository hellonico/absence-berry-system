docker-base:
	docker build -f dockers/base -t my-clojure-app .

docker-ring:
	docker build -f dockers/ring -t absence-ring .

docker-receive:
	docker build -f dockers/receive -t absence-receive .

start-ring:
	docker run -it -v `pwd`/db:/usr/src/app/db --restart=always -p 3000:3000 --name absence-ring absence-ring

start-receive:
	docker run -it -v `pwd`/db:/usr/src/app/db --restart=always --name absence-receive absence-receive