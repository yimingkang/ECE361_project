CC=javac
OBJS=Listener.java CCClient.java RoutingClient.java MainClient.java

all:
	$(CC) $(OBJS)

clean:
	rm -f *class
