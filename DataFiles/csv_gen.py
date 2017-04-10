import sys

def parse_histogram(data, output_prefix):
    of = open("graph_data/" + output_prefix + "_Hist.csv", 'w')

    hist_data = dict()
    for byte in data:
        if byte not in hist_data:
            hist_data[byte] = 1
        else:
            hist_data[byte] += 1

    for key in hist_data:
        of.write("%d, %d\n" % (key, hist_data[key]))

    of.close()

def parse_time_delayed(data, output_prefix):
    data = data[0: 2** 18]
    of = open("graph_data/" + output_prefix + "_TimeDelayed.csv", 'w')

    x,y,z = 0,0,0

    for i in range(3, len(data)):
        x = data[i] - data[i-1]
        y = data[i-1] - data[i-2]
        z = data[i-2] - data[i-3]

        try:
            of.write("%d, %d, %d\n" %(x, y, z))
        except Exception as e:
            print(i)
            print(x)
            print(y)
            print(z)
            print(e)

    of.close()


def create_3d(data, output_prefix):
    data = data[0: 2** 17]
    of = open("graph_data/UtilRandom_3d.csv", 'w')

    for i in range(0, len(data)-3, 3):
        x = data[i]
        y = data[i+1]
        z = data[i+2]

        try:
            of.write("%d, %d, %d\n" %(x, y, z))
        except Exception as e:
            print(i)
            print(x)
            print(y)
            print(z)
            print(e)

    of.close()

if __name__ == "__main__":
    file_name = sys.argv[1]
    data = open(sys.argv[1], 'rb').read()

    #output_prefix = file_name[:-4]
    #parse_histogram(data, output_prefix)
    #parse_time_delayed(data, output_prefix)
    create_3d(data, "graph_data/")

