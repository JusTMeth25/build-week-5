import { motion } from 'framer-motion'
import type { ButtonHTMLAttributes, HTMLAttributes, InputHTMLAttributes, ReactNode, TextareaHTMLAttributes } from 'react'
import { cn } from '@/lib/utils'

export function Button({ className, variant = 'primary', ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'ghost' | 'glass' | 'danger' }) {
  return <button className={cn('inline-flex items-center justify-center gap-2 rounded-2xl px-5 py-3 text-sm font-bold transition disabled:cursor-not-allowed disabled:opacity-50', variant === 'primary' && 'bg-white text-slate-950 shadow-[0_0_40px_rgba(255,255,255,.28)] hover:-translate-y-0.5 hover:bg-cyan-100', variant === 'ghost' && 'border border-white/10 bg-white/5 text-white hover:bg-white/10', variant === 'glass' && 'border border-cyan-300/30 bg-cyan-300/10 text-cyan-50 hover:bg-cyan-300/20', variant === 'danger' && 'border border-rose-300/30 bg-rose-500/15 text-rose-100 hover:bg-rose-500/25', className)} {...props} />
}

export function Card({ children, className, ...props }: HTMLAttributes<HTMLDivElement> & { children: ReactNode }) {
  return <div className={cn('rounded-[2rem] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/20 backdrop-blur-xl', className)} {...props}>{children}</div>
}

export function Field(props: InputHTMLAttributes<HTMLInputElement>) {
  return <input {...props} className={cn('w-full rounded-2xl border border-white/10 bg-white/10 px-4 py-3 text-sm text-white outline-none placeholder:text-slate-400 focus:border-cyan-300/70', props.className)} />
}

export function Area(props: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea {...props} className={cn('min-h-32 w-full rounded-2xl border border-white/10 bg-white/10 px-4 py-3 text-sm text-white outline-none placeholder:text-slate-400 focus:border-cyan-300/70', props.className)} />
}

export function Badge({ children, className }: { children: ReactNode; className?: string }) {
  return <span className={cn('inline-flex items-center gap-1 rounded-full border border-white/10 bg-white/10 px-3 py-1 text-xs font-bold text-slate-200', className)}>{children}</span>
}

export function Reveal({ children, delay = 0, className }: { children: ReactNode; delay?: number; className?: string }) {
  return <motion.div className={className} initial={{ opacity: 0, y: 24 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true, margin: '-80px' }} transition={{ duration: 0.55, delay }}>{children}</motion.div>
}