CC=javac
OBJS=Listener.java CCClient.java RoutingClient.java

all:
	$(CC) $(OBJS)

clean:
	rm -f *class
