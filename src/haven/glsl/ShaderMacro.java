/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven.glsl;

import haven.*;
import java.util.*;
import java.io.*;
import haven.GLShader.VertexShader;
import haven.GLShader.FragmentShader;

public interface ShaderMacro {
    public void modify(ProgramContext prog);

    public static class Program extends GLProgram {
	public transient final ProgramContext built;

	private static Collection<GLShader> build(ProgramContext prog) {
	    Collection<GLShader> ret = new LinkedList<GLShader>();
	    StringWriter fbuf = new StringWriter();
	    prog.fctx.construct(fbuf);
	    ret.add(new FragmentShader(fbuf.toString()));
	    StringWriter vbuf = new StringWriter();
	    prog.vctx.construct(vbuf);
	    ret.add(new VertexShader(vbuf.toString()));
	    return(ret);
	}

	public Program(ProgramContext ctx) {
	    super(build(ctx));
	    this.built = ctx;
	}

	public static Program build(Collection<ShaderMacro> mods) {
	    ProgramContext prog = new ProgramContext();
	    for(ShaderMacro mod : mods)
		mod.modify(prog);
	    return(new Program(prog));
	}

	public void dispose() {
	    synchronized(this) {
		super.dispose();
		umap.clear();
		amap.clear();
	    }
	}

	/* XXX: It would be terribly nice to replace these with some faster operation. */
	private final Map<Uniform, Integer> umap = new IdentityHashMap<Uniform, Integer>();
	public int uniform(Uniform var) {
	    Integer r = umap.get(var);
	    if(r == null) {
		String nm = built.symtab.get(var.name);
		if(nm == null)
		    throw(new ProgramException("Uniform not found in symtab: " + var, this, null));
		umap.put(var, r = new Integer(uniform(nm)));
	    }
	    return(r.intValue());
	}
	private final Map<Uniform, Integer> amap = new IdentityHashMap<Uniform, Integer>();
	public int attrib(Uniform var) {
	    Integer r = amap.get(var);
	    if(r == null) {
		String nm = built.symtab.get(var.name);
		if(nm == null)
		    throw(new ProgramException("Attribute not found in symtab: " + var, this, null));
		amap.put(var, r = new Integer(attrib(nm)));
	    }
	    return(r.intValue());
	}
    }
}
