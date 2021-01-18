from mininet.topo import Topo

class MyTopo(Topo):

    def __init__(self):

        Topo.__init__(self)

        LEOs = []
        for L in range(1, 67):
            LEO = self.addSwitch('S-%s' %L)
            LEOs.append(LEO)



        LEO1 = []
        LEO2 = []
        LEO3 = []
        LEO4 = []
        LEO5 = []
        LEO6 = []

        for LEO in LEOs:
            n = LEOs.index(LEO)
            l = len(LEOs)
            if n <= 10:
                LEO1.append(LEOs[n])
            elif n <= 21:
                LEO2.append(LEOs[n])
            elif n <= 32:
                LEO3.append(LEOs[n])
            elif n <= 43:
                LEO4.append(LEOs[n])
            elif n <= 54:
                LEO5.append(LEOs[n])
            else:
                LEO6.append(LEOs[n])

            for z in range(n + 1, l):
                x = z - n
                if x == 11:
                    self.addLink(LEO, LEOs[z])
                elif x == 55:
                    self.addLink(LEO, LEOs[z])

        LEOMap = {1: LEO1, 2: LEO2, 3: LEO3, 4: LEO4, 5: LEO5, 6: LEO6}

        for i in range(1, 7):
            list = LEOMap[i]
            for LEO in list:
                n = list.index(LEO)
                l = len(list)
                for z in range(n + 1, l):
                    x = z - n
                    if x == 1:
                        self.addLink(LEO, list[z])
                    elif x == 10:
                        self.addLink(LEO, list[z])



topos = {'mytopo': (lambda: MyTopo())}