import java.util.*;
public class MDS {
    private final TreeMap<Integer, Integer> tree = new TreeMap<>();//Used for forward mapping, mapping ID with price
    private final HashMap<Integer, TreeSet<Integer>> hash = new HashMap<>();//Reverse mapping, <Value, Key=TreeSet>, used to map
    //each int in description to an ID, Ex. description #123 = {ID#'s 12, 3, 44}
    private final HashMap<Integer, HashMap<Integer, Integer>> descCounts = new HashMap<>();//Map used to keep track of the duplicate values
    //of desc. <desc, <id, #ofDuplicates>>


    // Constructors
    public MDS() {
    }

    /* Public methods of MDS. Do not change their signatures.
       __________________________________________________________________
       a. Insert(id,price,list): insert a new item whose description is given
       in the list.  If an entry with the same id already exists, then its
       description and price are replaced by the new values, unless list
       is null or empty, in which case, just the price is updated.
       Returns 1 if the item is new, and 0 otherwise.
    */
    public int insert(int id, int price, java.util.List<Integer> list) {
        if(tree.containsKey(id)) {//id already exists within tree
            if(list == null || list.isEmpty()) {//list is null or empty so we only update the price
                tree.put(id, price);
                return 0;//item is already in the tree, so return 0
            }
            //list is not null or empty so we update the price and description
            tree.put(id, price);//update id with new price
            for (Map.Entry<Integer, TreeSet<Integer>> entry : new HashMap<>(hash).entrySet()) {//first we remove id from each desc value to later be replaced
                TreeSet<Integer> set = entry.getValue();//set contains the TreeSet of each key in hash
                Integer desc = entry.getKey();//Store key of associated set to be used for descCounts

                if(set.remove(id) && set.isEmpty()){//remove the id from this description value
                    hash.remove(desc);//We have no id's associated with that desc value, and so we remove it from hash
                }

                HashMap<Integer, Integer> countsMap = descCounts.get(desc);
                if(countsMap != null) {
                    countsMap.remove(id);
                    if(countsMap.isEmpty()) {
                        descCounts.remove(desc);
                    }
                }

            }
            for (Integer desc : list) {//Now we add id to each TreeSet of desc value in hash
                hash.computeIfAbsent(desc, k -> new TreeSet<>()).add(id);//if desc value already exists, we add id to its TreeSet
                //otherwise, a new TreeSet is created for the desc value and id is added
                descCounts.computeIfAbsent(desc, k -> new HashMap<>()).merge(id, 1, Integer::sum);//If desc already exists within descCounts, we increment the
                //duplicate count of id by 1, if the map does not exist, a map is created with <id, 1>
            }
            return 0;//id already existed so we return 0
        }
        tree.put(id, price);//id does not exist so we put it into the tree
        if(list != null && !list.isEmpty()) {//the item has a description so we need to update hash with the desc values
            for (Integer desc : list) {//iterate the list,
                hash.computeIfAbsent(desc, k -> new TreeSet<>()).add(id);//to add id to each desc value of hash, if the TreeSet is Absent, compute one
                descCounts.computeIfAbsent(desc, k -> new HashMap<>()).merge(id, 1, Integer::sum);//If desc already exists within descCounts, we increment the
                //duplicate count of id by 1, if the map does not exist, a map is created with <id, 1>
            }
        }
        return 1;//we added a new item so, return 1
    }

    // b. Find(id): return price of item with given id (or 0, if not found).
    public int find(int id) {
        return tree.getOrDefault(id, 0);//we return the value of the id price or default to return 0
    }

    /*
       c. Delete(id): delete item from storage.  Returns the sum of the
       numbers that are in the description of the item deleted,
       or 0, if such an id did not exist.
    */
    public int delete(int id) {
        int sum = 0;
        if(!tree.containsKey(id)) {//id does not exist so we return 0
            return 0;
        }

        for (Map.Entry<Integer, HashMap<Integer, Integer>> entry : new HashMap<>(descCounts).entrySet()) {//We iterate over each entrySet of descCounts, using a new HashMap,
            Integer desc = entry.getKey();//to avoid throwing ConcurrentModificationException due to altering a map during loop cycle
            HashMap<Integer, Integer> dupMap = entry.getValue();

            if (dupMap.containsKey(id)) {//id exists with desc number
                Integer dup = dupMap.get(id);//#ofDuplicates of id
                sum += desc * dup;//sum += desc * #ofDuplicates
                dupMap.remove(id);//remove id from desc number
            }
            if (dupMap.isEmpty()) {//we now have an empty desc number
                descCounts.remove(desc);//and should delete it from descCounts
            }
            TreeSet<Integer> set = hash.get(desc);
            if(set != null) {
                set.remove(id);//remove id from desc set of hash
                if(set.isEmpty()) {//set is now empty so we need to remove the desc number from hash
                    hash.remove(desc);
                }
            }
        }
        tree.remove(id);//remove id from tree as well
        return sum;//return the sum of all desc numbers associated with the deleted id
    }

    /*
       d. FindMinPrice(n): given an integer, find items whose description
       contains that number (exact match with one of the ints in the
       item's description), and return lowest price of those items.
       Return 0 if there is no such item.
    */
    public int findMinPrice(int n) {
        TreeSet<Integer> set = hash.get(n);//retrieve the set associated with the desc value of x
        if (set == null || set.isEmpty()) {//if the set is null or empty
            return 0;//we return 0
        }
        int lowest = Integer.MAX_VALUE;//initialize lower as the maximum value to compare with all id prices in the set
        for(Integer id : set) {//iterate through all id's in the set
            Integer price = tree.get(id);//get the price associated with the id
            if(price == null) {//encountered a method call of id with no price value associated with it
                return 0;
            }
            if (price < lowest) {//compare the price with the one currently valued as lowest
                lowest = price;//update lowest because we have found a lower price
            }
        }
        return lowest;//return the minimum price
    }

    /*
       e. FindMaxPrice(n): given an integer, find items whose description
       contains that number, and return highest price of those items.
       Return 0 if there is no such item.
    */
    public int findMaxPrice(int n) {
        TreeSet<Integer> set = hash.get(n);//retrieve the set associated with the desc value of n
        if (set == null || set.isEmpty()) {//if the set is null or empty
            return 0;//we return 0
        }
        int highest = Integer.MIN_VALUE;//initialize highest as the minimum value to compare with all id prices in the set
        for(Integer id : set) {//iterate through all id's in the set
            Integer price = tree.get(id);//get the price associated with the id
            if (price == null) {//encountered a method call of id with no price value associated with it
                return 0;
            }
            if (price > highest) {//compare the price with the one currently valued as highest
                highest = price;//update highest because we have found a higher price
            }
        }
        return highest;//return the maximum price
    }

    /*
       f. FindPriceRange(n,low,high): given int n, find the number
       of items whose description contains n, and in addition,
       their prices fall within the given range, [low, high].
    */
    public int findPriceRange(int n, int low, int high) {
        TreeSet<Integer> set = hash.get(n);//get all id's associated with desc n
        if (set == null || set.isEmpty()) {//set is empty so,
            return 0;//return 0
        }
        int count = 0;//variable used to count all id's within the given price range
        for(Integer id : set) {//iterate through set to compare price values
            Integer price = tree.get(id);//retrieve prive value of id
            if(price == null) {//encountered a method call of id with no price value associated with it
                return 0;
            }
            if (price >= low && price <= high) {//compare price to given range
                ++count;//increment count because we have found an item in range
            }
        }
        return count;//return number of items in range
    }

    /*
      g. RemoveNames(id, list): Remove elements of list from the description of id.
      It is possible that some of the items in the list are not in the
      id's description.  Return the sum of the numbers that are actually
      deleted from the description of id.  Return 0 if there is no such id.
    */
    public int removeNames(int id, java.util.List<Integer> list) {
        if(!tree.containsKey(id)) {//tree does not contain id so,
            return 0;//we return 0
        }
        int sum = 0;//variable to hold sum of desc values removed
        for(Integer desc : list){//iterate through list of desc values to be removed
            TreeSet<Integer> set = hash.get(desc);//get set of values associated with desc number
            HashMap<Integer, Integer> countsMap = descCounts.get(desc);//get of id's and duplicates
            if(set == null || countsMap == null){//the set or countsMap is empty or null so continue iterating through list
                continue;
            }
            if(countsMap.containsKey(id)) {//we found a desc number associated with id so,
                countsMap.remove(id);//we remove it from the map
                sum += desc;//sum == sum of desc values removed
                if(countsMap.isEmpty()) {//the set is now empty so,
                    descCounts.remove(desc);//we remove it from hash
                }
            }

            if(set.remove(id) && set.isEmpty()) {//if id is removed and the set is now empty,
                hash.remove(desc);//we need to remove the empty set from hash
            }

        }
        return sum;//return the sum of desc values that were removed
    }
}

