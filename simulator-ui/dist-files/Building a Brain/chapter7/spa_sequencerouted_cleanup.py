from spa import *

class Rules:
    def start(vision='LETTER'):
        set(state=vision)
    def A(state='A'):
        set(state='B')
    def B(state='B'):
        set(state='C')
    def C(state='C'):
        set(state='D')
    def D(state='D'):
        set(state='E')
    def E(state='E'):
        set(state='A')
    


class Routing(SPA):
    dimensions=16

    state=Buffer()
    vision=Buffer(feedback=0)
    BG=BasalGanglia(Rules)
    thal=Thalamus(BG)

    input=Input(10, vision='0.8*LETTER+D')

model=Routing()

# Create the clean-up memory.
import hrr
vocab = hrr.Vocabulary.defaults[model.dimensions] # get the vocabulary used by the rest of the network
pd = [vocab['A'].v.tolist()] # get a preferred direction vector aligned to the 'A' vector
cleanup = model.net.make('cleanup A', neurons=100, dimensions=1)
model.net.connect(model.state.net.network.getOrigin('state'), cleanup, transform=pd)