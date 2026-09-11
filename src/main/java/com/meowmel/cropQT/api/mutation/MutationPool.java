package com.meowmel.cropQT.api.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 一组「同类」作物，用于<b>没有确定性配方命中时</b>的随机杂交。
 *
 * <p>命中条件是：参与者里至少有两株属于本池。两株草莓放在一起，虽然表里没有
 * 「草莓 × 草莓」的配方，也能随机杂交出这个池里的另一种浆果。
 *
 * <p>要求「至少两株」而不是「至少一株」是有意的——否则一株池内作物配一株无关作物
 * 也能触发，等于把整个池变成万能杂交机。
 */
public class MutationPool {

    private final String name;
    private final Set<String> members = new LinkedHashSet<>();

    public MutationPool(String name) {
        this.name = name;
    }

    /** 往池里加一种作物（链式）。 */
    public MutationPool add(String cropId) {
        members.add(cropId);
        return this;
    }

    /** 批量加（链式）。 */
    public MutationPool addAll(List<String> cropIds) {
        members.addAll(cropIds);
        return this;
    }

    /** 参与者里至少有两株是本池成员时命中。 */
    public boolean matches(List<String> participants) {
        int present = 0;
        for (String id : participants) {
            if (members.contains(id) && ++present >= 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从池里随机挑一个成员。
     *
     * @return 作物 id；池为空时返回 {@code null}
     */
    public String pickRandom(Random rand) {
        if (members.isEmpty()) {
            return null;
        }
        int target = rand.nextInt(members.size());
        int index = 0;
        for (String member : members) {
            if (index++ == target) {
                return member;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    /** 池成员，按登记顺序。 */
    public List<String> getMembers() {
        return Collections.unmodifiableList(new ArrayList<>(members));
    }

    @Override
    public String toString() {
        return "MutationPool[" + name + " x" + members.size() + "]";
    }
}
