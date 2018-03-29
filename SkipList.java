// Cullen Bair
// cu326578
// COP 3503

import java.util.*;

class Node<T>
{
    // the parts of my node
    int height;
    T value;
    ArrayList<Node<T>> next = new ArrayList<>();

    // constructor #1 with only height mainly for the head
    Node(int height)
    {
        this.height = height;
        value = null;
        for(int i = 0; i < height; i++)
        {
            next.add(null);
        }
    }

    // constructor #2 for inserting new nodes
    Node(T data, int height)
    {
        value = data;
        this.height = height;
        for(int i = 0; i < height; i++)
        {
            next.add(null);
        }
    }

    public T value()
    {
        return value;
    }

    public int height()
    {
        return height;
    }

    public Node<T> next(int level)
    {
        if(level < 0 || level > height-1)
            return null;

        return next.get(level);
    }

    // very helpful for inserting/deleting. Setting the next value at that level
    // equal to the node given
    public void setNext(int level, Node<T> node)
    {
        next.set(level, node);
    }

    // simply increasing the given node by one
    public void grow()
    {
        next.add(null);
        height++;
    }

    // 50/50 chance at growing
    public void maybeGrow()
    {
        if(Math.random() < .5)
            grow();
    }

    // removing the top level of the node and decrementing the height
    public void trim()
    {
        next.remove(--height);
    }
}

// generic skiplist
public class SkipList<T extends Comparable<T>>
{
    // only two things i need to keep track of
    Node<T> head;
    int size;

    // constructor to initialize the list
    SkipList()
    {
        head = new Node<>(1);
        size = 0;
    }

    // constructor to initialize the list to a certain height
    SkipList(int height)
    {
        head = new Node<>(height);
        size = 0;
    }

    public int size()
    {
        return size;
    }

    public int height()
    {
        return head.height;
    }

    public Node<T> head()
    {
        return head;
    }

    // when we want to insert data with a random height
    public void insert(T data)
    {
        // check to see if the log[2]n is going to outgrow my height and preform
        // the growth before the insertion
        if(Math.ceil(Math.log(++size)/Math.log(2)) > head.height)
        {
            growSkipList();
        }
        
        // making a net to catch the nodes where we decrement the level
        ArrayList<Node<T>> net = new ArrayList<>();
        Node<T> temp = head;
        int level = head.height-1;
        
        // if the node is pointing to null, we want to go down before comparing null
        // to anything. if the data is bigger than the node's value we are pointing to,
        // we will make that our new node, otherwise if its less than or equal to we
        // go down and put that into our net
        while(level >= 0)
        {
            if(temp.next(level) == null)
            {
                net.add(temp);
                level--;
            }
            else if(temp.next(level).value().compareTo(data) < 0)
            {
                temp = temp.next(level);
            }
            else
            {
                net.add(temp);
                level--;
            }
        }
        
        // random height because we werent given one
        int newHeight = generateRandomHeight(head.height);
        
        // seen array to determine if we need to go down a level from the temporary
        // node's height that we are on
        ArrayList<Node<T>> seen = new ArrayList<>();
        Node<T> newNode = new Node<>(data, newHeight);

        // for each node in the net, determine if we need to set the next node equal
        // to what we are inserting
        for(Node<T> butterfly : net)
        {
            if(seen.contains(butterfly))
            {
                seen.add(butterfly);
            }
            else
            {
                seen.clear();
                seen.add(butterfly);
            }

            int cLevel = butterfly.height-(seen.size());
            
            if(cLevel <= newHeight-1)
            {
                newNode.setNext(cLevel, butterfly.next(cLevel));
                butterfly.setNext(cLevel, newNode);
            }
        }
    }

    // inserting for when we want to give the node a certain height. exactly the
    // same as the other insert method except the height will not be random
    public void insert(T data, int height)
    {
        // determine if we want to grow before the insertion
        if(Math.ceil(Math.log(++size)/Math.log(2)) > head.height)
            growSkipList();
        
        ArrayList<Node<T>> net = new ArrayList<>();
        Node<T> temp = head;
        int level = head.height-1;
        
        while(level >= 0)
        {
            if(temp.next(level) == null)
            {
                net.add(temp);
                level--;
            }
            else if(temp.next(level).value().compareTo(data) < 0)
            {
                temp = temp.next(level);
            }
            else
            {
                net.add(temp);
                level--;
            }
        }

        ArrayList<Node<T>> seen = new ArrayList<>();
        Node<T> newNode = new Node<>(data, height);

        for(Node<T> butterfly : net)
        {
            if(seen.contains(butterfly))
            {
                seen.add(butterfly);
            }
            else
            {
                seen.clear();
                seen.add(butterfly);
            }

            int cLevel = butterfly.height-(seen.size());
            
            if(cLevel <= height-1)
            {
                newNode.setNext(cLevel, butterfly.next(cLevel));
                butterfly.setNext(cLevel, newNode);
            }
        }  
    }

    // for deleting a specific element. much of the searching is the same as in insertion
    // but when we find the element, we delete insead of inserting anything
    public void delete(T data)
    {
        boolean found = false;
        ArrayList<Node<T>> net = new ArrayList<>();
        Node<T> temp = head;
        int level = head.height-1;
        
        while(level >= 0)
        {
            if(temp.next(level) == null)
            {
                net.add(temp);
                level--;
            }
            else if(temp.next(level).value().compareTo(data) < 0)
            {
                temp = temp.next(level);
            }
            else
            {
                net.add(temp);
                level--;
            }
        }

        // this checks to make sure we arent looking at null, and then determines
        // if the next element is the data. because of how the search algorithm is 
        // set up, it ends with the data being the node itself or the node right in 
        // front of it. if this next element is the data, we set temp equal to it
        if(temp.next(0) != null)
            if(temp.next(0).value().compareTo(data) == 0)
                temp = temp.next(0);
        
        // make sure we arent the head, otherwise we would be referencing null
        if(temp == head)
            return;
        
        // if we found the value to be deleted, we can carry on with the deletion
        if(temp.value().compareTo(data) == 0)
            found = true;

        
        if(found)
        {
            // same as insertion
            ArrayList<Node<T>> seen = new ArrayList<>();

            for(Node<T> butterfly : net)
            {
                if(seen.contains(butterfly))
                {
                    seen.add(butterfly);
                }
                else
                {
                    seen.clear();
                    seen.add(butterfly);
                }

                int cLevel = butterfly.height-(seen.size());

                if(cLevel <= temp.height()-1)
                {
                    // we just jump over the data and ignore it to delete it from the list
                    butterfly.setNext(cLevel, temp.next(cLevel));
                }
            }
                
            size--;
            
            // check to see if we should trim the list after we have successfully deleted something
            while(Math.ceil(Math.log(size)/Math.log(2)) < head.height && head.height > 1)
                trimSkipList();
        }
    }

    // determines if the list contains an element, using the same searching algorithms
    // as we have for insertion and deletion
    public boolean contains(T data)
    {
        Node<T> temp = head;
        boolean found = false;
        int level = head.height-1;
        
        while(level >= 0)
        {
            if(temp.next(level) == null)
            {
                level--;
            }
            else if(temp.next(level).value().compareTo(data) < 0)
            {
                temp = temp.next(level);
            }
            else
            {
                level--;
            }
        }
        
        // insead of finding out which node has the data, we just look at both 
        // values and if either of them have data, the list contains data
        if(temp.next(0).value().compareTo(data) == 0 || temp.value().compareTo(data) == 0)
            found = true;
        
        return found;
    }

    // gets a certain node with data as its value, using the same searching
    // algorithm as before
    public Node<T> get(T data)
    {
        Node<T> temp = head;
        int level = head.height-1;
        
        while(level >= 0)
        {
            if(temp.next(level) == null)
            {
                level--;
            }
            if(temp.next(level).value().compareTo(data) < 0)
            {
                temp = temp.next(level);
            }
            else
            {
                level--;
            }
        }
        
        if(temp.next(0).value().compareTo(data) == 0)
            return temp.next(0);
        else if(temp.value().compareTo(data) == 0)
            return temp;
        else
            return null;
    }

    // generates a height for insertion, 50% to get 1, 25% to get 2, etc.
    private static int generateRandomHeight(int maxHeight)
    {
        for(int i = 1; i <= maxHeight; i++)
        {
            if(Math.random() < .5)
                return i;
        }
        return maxHeight;
    }

    // if the list is too small and we need to increase the heights
    private void growSkipList()
    {
        // we need an array for all the nodes that get increased because they need
        // to point to each other once the growing is done
        ArrayList<Node<T>> bigBoys = new ArrayList<>();

        Node<T> temp = head;

        // head for sure grows, all the others is 50% chance
        head.grow();
        bigBoys.add(head);
        
        // for each node at head height, give 50% chance to grow
        while(temp.next(head.height()-1) != null)
        {
            temp = temp.next(head.height()-1);
            
            temp.maybeGrow();
            
            if(temp.height() == head.height())
                bigBoys.add(temp);
        }
        
        // all the nodes that get increased, point to each other rather than null
        for(int i = 0; i < bigBoys.size()-1; i++)
        {
            bigBoys.get(i).setNext(head.height-1, bigBoys.get(i+1));
        }
    }

    // trimming the list if it's too tall, being careful not to lose any nodes
    // by using two temp nodes to hold my list in tact while im trimming
    private void trimSkipList()
    {
        Node<T> temp = head;
        Node<T> temp2;
        
        while(temp != null)
        {
            temp2 = temp.next(head.height-1);
            temp.trim();
            temp = temp2;

        }
    }

    public static double difficultyRating()
    {
        return 4.3;
    }

    public static double hoursSpent()
    {
        return 22.0;
    }
}
